const mongoose = require('mongoose');

const dataSchema = new mongoose.Schema({
  type: String,
  data: mongoose.Schema.Types.Mixed,
  car: { type: mongoose.Schema.Types.ObjectId, ref: 'Car' },
}, { timestamps: true });

const Data = mongoose.model('Data', dataSchema);

module.exports = Data;
