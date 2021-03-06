import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

class TreeTypeService extends AbstractService {

  const
  getApiPath() {
    return '/tree-types';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.name;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('name');
  }
}

export default TreeTypeService;
