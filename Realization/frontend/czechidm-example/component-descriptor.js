
module.exports = {
  'id': 'example',
  'name': 'Example',
  'description': 'Components for Example module',
  'components': [
    {
      'id': 'exampleDashboard',
      'type': 'dashboard',
      'span': '4',
      'order': '4',
      'component': require('./src/content/dashboards/ExampleDashboard')
    }
  ]
};
