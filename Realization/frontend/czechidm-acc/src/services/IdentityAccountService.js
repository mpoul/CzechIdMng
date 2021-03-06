import { Services } from 'czechidm-core';
import { Domain } from 'czechidm-core';
import AccountTypeEnum from '../domain/AccountTypeEnum';

export default class IdentityAccountService extends Services.AbstractService {

  constructor() {
    super();
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${AccountTypeEnum.getNiceLabel(entity.account.accountType)}:${entity.account._embedded.system.name}:${entity.account._embedded.systemEntity ? entity.account._embedded.systemEntity.uid : ''}`;
  }

  getApiPath() {
    return '/identity-accounts';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('account.uid');
  }
}
