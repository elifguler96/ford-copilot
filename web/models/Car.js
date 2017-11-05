const mongoose = require('mongoose');

const carSchema = new mongoose.Schema({
  key: String,
  data: [{ type: mongoose.Schema.Types.ObjectId, ref: 'Data' }],
  user: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
}, { timestamps: true });

const Car = mongoose.model('Car', carSchema);

module.exports = Car;
