const mongoose = require('mongoose');

const contactSchema = new mongoose.Schema({
  number: String,
  text: String,
});

const EmergencyContact = mongoose.model('EmergencyContact', contactSchema);

module.exports = EmergencyContact;
