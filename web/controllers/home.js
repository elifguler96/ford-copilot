/**
 * GET /
 * Home page.
 */
exports.index = (req, res) => {
  console.log(req.isAuthenticated());
  res.render(req.isAuthenticated() ? 'home_account' : 'home', {
    title: 'Home',
    cars: [{
      type: 'success',
      title: 'Company Car 1',
      subtitle: 'Gray Ford Focus, 2016',
      rows: [
        { status: 'green', text: 'Speed', value: '58 km/h' },
        { status: 'green', text: 'Speed Limit', value: '70 km/h' },
        { status: 'green', text: 'Fuel', value: '83%' },
        { status: 'green', text: 'Last Refuel', value: 'Opet, 5/11/2017, 07:50' },
        { status: 'green', text: 'Fuel Efficiency', value: '120 km/lt' },
      ],
    }, {
      type: 'danger',
      title: 'Company Car 2',
      subtitle: 'Gray Ford Focus, 2015',
      rows: [
        { status: 'green', text: 'Speed', value: '97 km/h' },
        { status: 'green', text: 'Speed Limit', value: '120 km/h' },
        { status: 'red', text: 'Fuel', value: '15%' },
        { status: 'red', text: 'Last Refuel', value: 'PO, 4/11/2017, 18:41' },
        { status: 'red', text: 'Fuel Efficiency', value: '88 km/lt' },
      ],
    }, {
      type: 'warning',
      title: 'Personal Car',
      subtitle: 'Yellow Ford Focus, 2014',
      rows: [
        { status: '', text: 'Speed', value: '0 km/h' },
        { status: '', text: 'Speed Limit', value: 'N/A' },
        { status: 'green', text: 'Fuel', value: '99%' },
        { status: 'green', text: 'Last Refuel', value: 'Shell, 3/11/2017, 17:38' },
        { status: 'green', text: 'Fuel Efficiency', value: '102 km/lt' },
      ],
    }, {
      type: 'warning',
      title: 'Personal Car - Wife',
      subtitle: 'Red Ford Focus, 2012',
      rows: [
        { status: 'yellow', text: 'Speed', value: '85 km/h' },
        { status: 'green', text: 'Speed Limit', value: '90 km/h' },
        { status: 'yellow', text: 'Fuel', value: '50%' },
        { status: 'green', text: 'Last Refuel', value: 'Shell, 2/11/2017, 10:55' },
        { status: 'green', text: 'Fuel Efficiency', value: '101 km/lt' },
      ],
    }]
  });
};
