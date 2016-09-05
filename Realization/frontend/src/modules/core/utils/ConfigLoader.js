import Immutable from 'immutable';
import _ from 'lodash';
//
import ModuleLoader from './ModuleLoader';
import config from '../../../../config.json'; // TODO: config has to be parametrized !!!

const moduleLoader = new ModuleLoader();

// suported and default values for navigation item in module descriptor
const ITEM_DEFAULTS = {
  'id': undefined,
  'type': 'DYNAMIC',
  'section': 'main',
  'label': undefined,
  'labelKey': undefined,
  'title': undefined,
  'titleKey': undefined,
  'icon': undefined,
  'order': 0,
  'priority': 0,
  'path': undefined,
  'access': [
    {
      'type': 'IS_AUTHENTICATED'
    }
  ],
  'items': []
};

/**
* Loads configuration
* TODO: redux action -> manager
*/
export default class ConfigLoader {

  constructor() {
  }

  /**
   * Returns config part by key or null
   */
  getConfig(key) {
    return config[key];
  }

  /**
   * Returns BE server base url
   * @return {string}
   */
  getServerUrl() {
    return this.getConfig('serverUrl');
  }

  /**
   * Returns enabled module ids
   *
   * @return {array[string]}
   */
  getEnabledModuleIds() {
    return moduleLoader.getEnabledModuleIds();
  }

  /**
   * Returns Module descriptor for given module id
   *
   * @param  {string} moduleId
   * @return {ModuleDescriptor} json object
   */
  getModuleDescriptor(moduleId) {
    const loaderModuleDescriptor = moduleLoader.getModuleDescriptor(moduleId);
    const configModuleDescriptor = this._getConfigModuleDescriptor(moduleId);
    // Merge module descriptor with override values from configuration
    return _.mergeWith(loaderModuleDescriptor, configModuleDescriptor, this._overrideModuleDescriptorMerge.bind(this));
  }

  _getConfigModuleDescriptor(moduleId) {
    if (config.overrideModuleDescriptor) {
      return config.overrideModuleDescriptor[moduleId];
    }
    return {};
  }

  /**
   * Function for lodash mergeWith. Is use for custom merge override module descriptors from configuration.
   */
  _overrideModuleDescriptorMerge(objValue, srcValue) {
    let standardMerge = false;
    if (_.isArray(objValue)) {
      for (const value of objValue) {
        for (const overrideValue of srcValue) {
          if (overrideValue && value && overrideValue.id && value.id && overrideValue.id === value.id) {
            _.mergeWith(value, overrideValue, this._overrideModuleDescriptorMerge.bind(this));
          }
          // Item not have id ... we will use standard merge for this array
          if (value && !value.id) {
            standardMerge = true;
          }
        }
      }
      if (!standardMerge) {
        // we did merge in this array itself. Return array as resutl.
        return objValue;
      }
      // standard merge
    }
  }


  /**
   * Append module navigation to items
   * - works with order, priority etc
   */
  _resolveNavigation(navigationItems, moduleId) {
    const moduleDescriptor = this.getModuleDescriptor(moduleId);

    if (!moduleDescriptor.navigation) {
      return this._navigationItems;
    }
    // items
    navigationItems = this._appendNavigationItems(navigationItems, '', moduleDescriptor.navigation.items);
    // routes access
    navigationItems = this._appendNavigationAccess(navigationItems, moduleDescriptor.navigation.access);
    //
    return navigationItems;
  }

  _appendNavigationItems(navigationItems, parentId, rawItems) {
    if (!rawItems) {
      return navigationItems;
    }
    //
    for (let i = 0; i < rawItems.length; i++) {
      // append default values
      const item = _.merge({ parentId }, ITEM_DEFAULTS, rawItems[i]);
      const _parentId = item.parentId || parentId;
      //
      let items = (navigationItems.get(ConfigLoader.NAVIGATION_BY_PARENT).has(_parentId)) ? navigationItems.get(ConfigLoader.NAVIGATION_BY_PARENT).get(_parentId) : new Immutable.Map();
      // first or higher priority wins
      if (!items.has(item.id) || items.get(item.id).priority < item.priority) {
        items = items.set(item.id, item);
        navigationItems = navigationItems.set(ConfigLoader.NAVIGATION_BY_ID, navigationItems.get(ConfigLoader.NAVIGATION_BY_ID).set(item.id, item));
      }
      navigationItems = navigationItems.set(ConfigLoader.NAVIGATION_BY_PARENT, navigationItems.get(ConfigLoader.NAVIGATION_BY_PARENT).set(_parentId, items));
      navigationItems = this._appendNavigationItems(navigationItems, item.id, item.items);
    }
    return navigationItems;
  }

  _appendNavigationAccess(navigationItems, rawAccess) {
    if (!rawAccess) {
      return navigationItems;
    }
    rawAccess.forEach(item => {
      let items = (navigationItems.get(ConfigLoader.NAVIGATION_BY_PATH).has(item.path)) ? navigationItems.get(ConfigLoader.NAVIGATION_BY_PATH).get(item.path) : new Immutable.List();
      items = items.push(item);
      navigationItems = navigationItems.set(ConfigLoader.NAVIGATION_BY_PATH, navigationItems.get(ConfigLoader.NAVIGATION_BY_PATH).set(item.path, items));
    });
    return navigationItems;
  }

  /**
   * Loads navigation items from module descriptors
   *
   * @return Immutable.Map({ byParent: Immutable.Map, byId: Immutable.Map. byPath: })
   */
  getNavigation() {
    let navigationItems = new Immutable.Map({
      [ConfigLoader.NAVIGATION_BY_PARENT]: new Immutable.Map({}),
      [ConfigLoader.NAVIGATION_BY_ID]: new Immutable.Map({}),
      [ConfigLoader.NAVIGATION_BY_PATH]: new Immutable.Map({})
    });
    moduleLoader.getEnabledModuleIds().map(moduleId => {
      navigationItems = this._resolveNavigation(navigationItems, moduleId);
    });
    // order
    navigationItems = navigationItems.set(
      ConfigLoader.NAVIGATION_BY_PARENT,
      navigationItems.get(ConfigLoader.NAVIGATION_BY_PARENT)
        .mapEntries(([k, v]) => [k, v.sortBy(item => item.order)])
    );
    //
    return navigationItems;
  }

  /**
   * Returns navigation items for given parent. If not parentId is suplied, then returns root navigation items
   * @return array of items
   */
  getNavigationItems(parentId = null) {
    // load all module descriptor
    const navigationItems = this.getNavigation().get(ConfigLoader.NAVIGATION_BY_PARENT);
    // level by parentId
    if (!parentId) {
      return navigationItems.get('').toArray();
    }
    if (!navigationItems.has(parentId)) {
      return [];
    }
    return navigationItems.get(parentId).toArray();
  }
}

ConfigLoader.NAVIGATION_BY_PARENT = 'byParent';
ConfigLoader.NAVIGATION_BY_ID = 'byId';
ConfigLoader.NAVIGATION_BY_PATH = 'byPath';