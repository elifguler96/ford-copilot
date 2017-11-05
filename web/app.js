/**
 * Module dependencies.
 */
const express = require('express');
const compression = require('compression');
const session = require('express-session');
const bodyParser = require('body-parser');
const logger = require('morgan');
const chalk = require('chalk');
const errorHandler = require('errorhandler');
const dotenv = require('dotenv');
const MongoStore = require('connect-mongo')(session);
const flash = require('express-flash');
const path = require('path');
const mongoose = require('mongoose');
const passport = require('passport');
const expressValidator = require('express-validator');
const expressStatusMonitor = require('express-status-monitor');
const sass = require('node-sass-middleware');
const multer = require('multer');

const upload = multer({ dest: path.join(__dirname, 'uploads') });

/**
 * Load environment variables from .env file, where API keys and passwords are configured.
 */
dotenv.load({ path: '.env.example' });

/**
 * Controllers (route handlers).
 */
const homeController = require('./controllers/home');
const userController = require('./controllers/user');
const apiController = require('./controllers/api');
const contactController = require('./controllers/contact');
const Car = require('./models/Car');
const User = require('./models/User');
const Data = require('./models/Data');
const EmergencyContact = require('./models/EmergencyContact');

/**
 * API keys and Passport configuration.
 */
const passportConfig = require('./config/passport');

/**
 * Create Express server.
 */
const app = express();

/**
 * Connect to MongoDB.
 */
mongoose.Promise = global.Promise;
mongoose.connect(process.env.MONGODB_URI || process.env.MONGOLAB_URI);
mongoose.connection.on('error', (err) => {
  console.error(err);
  console.log('%s MongoDB connection error. Please make sure MongoDB is running.', chalk.red('✗'));
  process.exit();
});

/**
 * Express configuration.
 */
app.set('host', process.env.OPENSHIFT_NODEJS_IP || '0.0.0.0');
app.set('port', process.env.PORT || process.env.OPENSHIFT_NODEJS_PORT || 8080);
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'pug');
app.use(expressStatusMonitor());
app.use(compression());
app.use(sass({
  src: path.join(__dirname, 'public'),
  dest: path.join(__dirname, 'public')
}));
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(expressValidator());
app.use(session({
  resave: true,
  saveUninitialized: true,
  secret: process.env.SESSION_SECRET,
  store: new MongoStore({
    url: process.env.MONGODB_URI || process.env.MONGOLAB_URI,
    autoReconnect: true,
    clear_interval: 3600
  })
}));
app.use(passport.initialize());
app.use(passport.session());
app.use(flash());
app.use((req, res, next) => {
  res.locals.user = req.user;
  next();
});
app.use((req, res, next) => {
  // After successful login, redirect back to the intended page
  if (!req.user &&
    req.path !== '/login' &&
    req.path !== '/signup' &&
    !req.path.match(/^\/auth/) &&
    !req.path.match(/\./)) {
    req.session.returnTo = req.path;
  } else if (req.user &&
    req.path === '/account') {
    req.session.returnTo = req.path;
  }
  next();
});
app.use(express.static(path.join(__dirname, 'public'), { maxAge: 31557600000 }));

/**
 * Primary app routes.
 */
app.get('/', homeController.index);
app.get('/login', userController.getLogin);
app.post('/login', userController.postLogin);
app.post('/api/login', passport.authenticate('local'), (req, res, next) => {
  if (!req.user) return next({ error: 'Failed to login.' });
  return res.json(req.user);
});
app.get('/logout', userController.logout);
app.get('/forgot', userController.getForgot);
app.post('/forgot', userController.postForgot);
app.get('/reset/:token', userController.getReset);
app.post('/reset/:token', userController.postReset);
app.get('/signup', userController.getSignup);
app.post('/signup', userController.postSignup);
app.get('/contact', contactController.getContact);
app.post('/contact', contactController.postContact);
app.get('/account', passportConfig.isAuthenticated, userController.getAccount);
app.post('/account/profile', passportConfig.isAuthenticated, userController.postUpdateProfile);
app.post('/account/password', passportConfig.isAuthenticated, userController.postUpdatePassword);
app.post('/account/delete', passportConfig.isAuthenticated, userController.postDeleteAccount);
app.get('/account/unlink/:provider', passportConfig.isAuthenticated, userController.getOauthUnlink);

/**
 * API examples routes.
 */
app.get('/api', apiController.getApi);
app.get('/api/aviary', apiController.getAviary);
app.get('/api/scraping', apiController.getScraping);
app.get('/api/clockwork', apiController.getClockwork);
app.post('/api/clockwork', apiController.postClockwork);
app.get('/api/facebook', passportConfig.isAuthenticated, passportConfig.isAuthorized, apiController.getFacebook);
app.get('/api/twitter', passportConfig.isAuthenticated, passportConfig.isAuthorized, apiController.getTwitter);
app.post('/api/twitter', passportConfig.isAuthenticated, passportConfig.isAuthorized, apiController.postTwitter);
app.get('/api/lob', apiController.getLob);
app.get('/api/upload', apiController.getFileUpload);
app.post('/api/upload', upload.single('myFile'), apiController.postFileUpload);
app.get('/api/google-maps', apiController.getGoogleMaps);


app.get('/auth/facebook', passport.authenticate('facebook', { scope: ['email', 'public_profile'] }));
app.get('/auth/facebook/callback', passport.authenticate('facebook', { failureRedirect: '/login' }), (req, res) => {
  res.redirect(req.session.returnTo || '/');
});
app.get('/auth/google', passport.authenticate('google', { scope: 'profile email' }));
app.get('/auth/google/callback', passport.authenticate('google', { failureRedirect: '/login' }), (req, res) => {
  res.redirect(req.session.returnTo || '/');
});
app.get('/auth/twitter', passport.authenticate('twitter'));
app.get('/auth/twitter/callback', passport.authenticate('twitter', { failureRedirect: '/login' }), (req, res) => {
  res.redirect(req.session.returnTo || '/');
});

// Configure our custom car-specific controllers
require('./controllers/speedLimit').configureRoutes(app);

app.get('/api/car', (req, res, next) => {
  Car.findOne({ _id: req.body.id })
    .populate('user')
    .exec((err, car) => {
      if (err) { return next(err); }
      return res.json(car);
    });
});

app.post('/api/car', (req, res, next) => {
  User.findOne({ _id: req.body.user }, (err, user) => {
    if (err) return next(err);

    const car = new Car({
      key: req.body.key,
      user: req.body.user,
    });

    car.save((err) => {
      if (err) { return next(err); }
      return res.json(car);
    });

    user.car = car._id;
    return res.json(car);
  });
});


const dataTypes = {
  'fuel-km': 1,
};

app.post('/api/log-data', (req, res, next) => {
  const { type, data, car } = req.body;
  if (!dataTypes[type]) {
    return next({ error: 'Undefined data type.' });
  }
  const dataObject = new Data({ type, data: JSON.parse(data), car });
  dataObject.save((err) => {
    if (err) return next(err);
    return res.status(204).send();
  });
});

app.post('/api/emergency-contact', (req, res, next) => {
  const { number, text } = req.body;
  EmergencyContact.findOne({}, (err, obj) => {
    if (obj) {
      console.log(obj);
      obj.number = number;
      obj.text = text;
      obj.save((err) => {
        if (err) return next(err);
        return res.json({ number: obj.number, text: obj.text });
      });
    } else {
      const contact = new EmergencyContact({ number, text });
      contact.save((err) => {
        if (err) return next(err);
        return res.json({ number: contact.number, text: contact.text });
      });
    }
  });
});

app.get('/api/emergency-contact', (req, res, next) => {
  EmergencyContact.findOne({}, (err, obj) => {
    if (err) return next(err);
    return res.json({ number: obj.number, text: obj.text });
  });
});

app.get('/api/send-emergency-text', (req, res, next) => {
  EmergencyContact.findOne({}, (err, obj) => {
    if (err) return next(0);
    const message = {
      to: '+905548154272',
      from: '+14804180232',
      body: obj.text.replace('%location', '40.975395, 29.234932'),
    };
    const twilio = require('twilio')(process.env.TWILIO_ACCOUNT_SID, process.env.TWILIO_AUTH_TOKEN); // eslint-disable-line
    twilio.api.messages.create(message).then((responseData) => {
      console.log(responseData);
      return res.json(1);
    }).catch((err) => {
      console.log(err);
      return res.json(0);
    });
  });
});

app.get('/api/refuel-time', (req, res, next) => {
  Data
    .find({ type: 'fuel-km' })
    .select('data')
    .exec((err, dataObjects) => {
      if (err) return next(err);
      const reduced = dataObjects
        .map(data => typeof data.data === 'object' ? data.data : JSON.parse(data.data))
        .filter(data => !!data)
        .sort((a, b) => a.km > b.km ? -1 : (a.km === b.km ? 0 : 1));
      if (!reduced.length) return res.json(-1);
      let temp = 99999999999999;
      let lastRefuelIndex;
      for (let i = 0; i < reduced.length; i++) {
        if (reduced[i].km <= temp) {
          temp = reduced[i].km;
        } else {
          lastRefuelIndex = i;
          break;
        }
      }

      if (lastRefuelIndex == null) {
        lastRefuelIndex = reduced.length - 1;
      }
      const s = (reduced[0].km - reduced[lastRefuelIndex].km) / (reduced[0].fuel - reduced[lastRefuelIndex].fuel);
      return res.json(reduced[0].fuel / Math.abs(s));
    });
});

/**
 * Error Handler.
 */
app.use(errorHandler());

/**
 * Start Express server.
 */
app.listen(app.get('port'), () => {
  console.log('%s App is running at http://localhost:%d in %s mode', chalk.green('✓'), app.get('port'), app.get('env'));
  console.log('  Press CTRL-C to stop\n');
});

module.exports = app;
