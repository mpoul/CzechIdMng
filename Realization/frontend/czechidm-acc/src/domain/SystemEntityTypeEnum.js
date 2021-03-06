import { Enums } from 'czechidm-core';

/**
 * OperationType for adit operation etc.
 */
export default class SystemEntityTypeEnum extends Enums.AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`acc:enums.SystemEntityTypeEnum.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }

  static findSymbolByKey(key) {
    return super.findSymbolByKey(this, key);
  }

  static getLevel(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      case this.IDENTITY: {
        return 'success';
      }
      case this.GROUP: {
        return 'primary';
      }
      default: {
        return 'default';
      }
    }
  }
}

SystemEntityTypeEnum.IDENTITY = Symbol('IDENTITY');
// SystemEntityTypeEnum.GROUP = Symbol('GROUP');
