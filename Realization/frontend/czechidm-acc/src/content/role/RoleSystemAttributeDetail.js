import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import { Basic, Utils, Domain} from 'czechidm-core';
import { RoleSystemAttributeManager, RoleSystemManager, SchemaAttributeHandlingManager} from '../../redux';

const uiKey = 'role-system-attribute';
const roleSystemAttributeManager = new RoleSystemAttributeManager();
const roleSystemManager = new RoleSystemManager();
const schemaAttributeHandlingManager = new SchemaAttributeHandlingManager();

class RoleSystemAttributeDetail extends Basic.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
  }

  getUiKey() {
    return uiKey;
  }

  getManager() {
    return roleSystemAttributeManager;
  }

  getContentKey() {
    return 'acc:content.role.roleSystemAttributeDetail';
  }

  componentWillReceiveProps(nextProps) {
    const { attributeId} = nextProps.params;
    if (attributeId && attributeId !== this.props.params.attributeId) {
      this._initComponent(nextProps);
    }
  }

  // Did mount only call initComponent method
  componentDidMount() {
    this._initComponent(this.props);
  }

  /**
   * Method for init component from didMount method and from willReceiveProps method
   * @param  {properties of component} props For didmount call is this.props for call from willReceiveProps is nextProps.
   */
  _initComponent(props) {
    const {roleSystemId, attributeId} = props.params;
    if (this._getIsNew(props)) {
      this.setState({attribute: {
        roleSystem: roleSystemId,
      },
      entityHandlingId: props.location.query.entityHandlingId});
    } else {
      this.context.store.dispatch(roleSystemAttributeManager.fetchEntity(attributeId));
    }
    this.selectNavigationItems(['roles', 'role-systems']);
  }

  _getIsNew(nextProps) {
    const { query } = nextProps ? nextProps.location : this.props.location;
    return (query) ? query.new : null;
  }

  save(event) {
    const formEntity = this.refs.form.getData();
    formEntity.roleSystem = roleSystemManager.getSelfLink(formEntity.roleSystem);
    formEntity.schemaAttributeHandling = schemaAttributeHandlingManager.getSelfLink(formEntity.schemaAttributeHandling);
    //
    super.save(formEntity, event);
  }

  afterSave(entity, error) {
    if (!error) {
      if (this._getIsNew()) {
        this.addMessage({ message: this.i18n('create.success', { name: entity.name }) });
        this.context.router.goBack();
      } else {
        this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
      }
      this.context.router.goBack();
    } else {
      this.addError(error);
    }
    super.afterSave();
  }

  closeDetail() {
    this.refs.form.processEnded();
  }

  _uidChanged(event) {
    const checked = event.currentTarget.checked;
    // I need set value direct to checkbox (this event is run befor state is set, but I need him in render mothod now)
    this.refs.uid.setState({value: checked}, () => {
      this.forceUpdate();
    });
  }

  _disabledChanged(key, event) {
    const checked = event.currentTarget.checked;
    // I need set value direct to checkbox (this event is run befor state is set, but I need him in render mothod now)
    this.refs[key].setState({value: checked}, () => {
      this.forceUpdate();
    });
  }

  _checkboxChanged(key, disableKey, event) {
    const checked = event.currentTarget.checked;
    // I need set value direct to checkbox (this event is run befor state is set, but I need him in render mothod now)
    if (checked) {
      this.refs[disableKey].setState({value: false});
    }
    this.refs[key].setState({value: checked}, () => {
      this.forceUpdate();
    });
  }

  _schemaAttributeChange(value) {
    if (!this.refs.name.getValue()) {
      this.refs.name.setValue(value.name);
    }
  }

  render() {
    const { _showLoading, _attribute, _systemEntityHandlingId} = this.props;
    const { entityHandlingId} = this.state;
    const isNew = this._getIsNew();
    const attribute = isNew ? this.state.attribute : _attribute;
    const _entityHandlingId = isNew ? entityHandlingId : _systemEntityHandlingId;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('entityHandlingId', _entityHandlingId ? _entityHandlingId : Domain.SearchParameters.BLANK_UUID);

    const _isDisabled = this.refs.disabledDefaultAttribute ? this.refs.disabledDefaultAttribute.getValue() : false;
    const _isEntityAttribute = this.refs.entityAttribute ? this.refs.entityAttribute.getValue() : false;
    const _isExtendedAttribute = this.refs.extendedAttribute ? this.refs.extendedAttribute.getValue() : false;
    const _showNoRepositoryAlert = (!_isExtendedAttribute && !_isEntityAttribute);

    const _isRequiredIdmField = (_isEntityAttribute || _isExtendedAttribute) && !_isDisabled;

    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.ContentHeader>
          <Basic.Icon value="list-alt"/>
          {' '}
          <span dangerouslySetInnerHTML={{ __html: this.i18n('header', attribute ? { name: attribute.name} : {})}}/>
        </Basic.ContentHeader>
        <form onSubmit={this.save.bind(this)}>
          <Basic.Panel className="no-border last">
            <Basic.AbstractForm ref="form" data={attribute} showLoading={_showLoading} className="form-horizontal">
              <Basic.Checkbox
                ref="disabledDefaultAttribute"
                onChange={this._disabledChanged.bind(this, 'disabledDefaultAttribute')}
                tooltip={this.i18n('acc:entity.RoleSystemAttribute.disabledDefaultAttributeTooltip')}
                label={this.i18n('acc:entity.RoleSystemAttribute.disabledDefaultAttribute')}/>
              <Basic.SelectBox
                ref="roleSystem"
                manager={roleSystemManager}
                label={this.i18n('acc:entity.RoleSystemAttribute.roleSystem')}
                readOnly
                required/>
              <Basic.SelectBox
                ref="schemaAttributeHandling"
                manager={schemaAttributeHandlingManager}
                onChange={this._schemaAttributeChange.bind(this)}
                forceSearchParameters={forceSearchParameters}
                label={this.i18n('acc:entity.RoleSystemAttribute.schemaAttributeHandling')}
                required/>
              <Basic.TextField
                ref="name"
                label={this.i18n('acc:entity.RoleSystemAttribute.name.label')}
                helpBlock={this.i18n('acc:entity.RoleSystemAttribute.name.help')}
                required
                max={255}/>
              <Basic.Checkbox
                ref="uid"
                readOnly = {_isDisabled}
                onChange={this._uidChanged.bind(this)}
                tooltip={this.i18n('acc:entity.SchemaAttributeHandling.uid.tooltip')}
                label={this.i18n('acc:entity.SchemaAttributeHandling.uid.label')}/>
              <Basic.Checkbox
                ref="extendedAttribute"
                onChange={this._checkboxChanged.bind(this, 'extendedAttribute', 'entityAttribute')}
                readOnly = {_isDisabled}
                label={this.i18n('acc:entity.SchemaAttributeHandling.extendedAttribute')}/>
              <Basic.Checkbox
                ref="entityAttribute"
                onChange={this._checkboxChanged.bind(this, 'entityAttribute', 'extendedAttribute')}
                readOnly = {_isDisabled}
                label={this.i18n('acc:entity.SchemaAttributeHandling.entityAttribute')}/>
              <Basic.Checkbox
                ref="confidentialAttribute"
                readOnly = {_isDisabled || !_isRequiredIdmField}
                label={this.i18n('acc:entity.SchemaAttributeHandling.confidentialAttribute')}/>
              <Basic.TextField
                ref="idmPropertyName"
                readOnly = {_isDisabled || !_isRequiredIdmField}
                label={this.i18n('acc:entity.SchemaAttributeHandling.idmPropertyName.label')}
                helpBlock={this.i18n('acc:entity.SchemaAttributeHandling.idmPropertyName.help')}
                required = {_isRequiredIdmField}
                max={255}/>
              <Basic.LabelWrapper label=" ">
                <Basic.Alert
                   rendered={_showNoRepositoryAlert}
                   key="no-repository-alert"
                   icon="exclamation-sign"
                   className="no-margin"
                   text={this.i18n('acc:content.system.attributeHandlingDetail.alertNoRepository')}/>
              </Basic.LabelWrapper>
              <Basic.ScriptArea
                ref="transformScript"
                readOnly = {_isDisabled}
                helpBlock={this.i18n('acc:entity.RoleSystemAttribute.transformScript.help')}
                label={this.i18n('acc:entity.RoleSystemAttribute.transformScript.label')}/>
            </Basic.AbstractForm>
            <Basic.PanelFooter>
              <Basic.Button type="button" level="link"
                onClick={this.context.router.goBack}
                showLoading={_showLoading}>
                {this.i18n('button.back')}
              </Basic.Button>
              <Basic.Button
                onClick={this.save.bind(this)}
                level="success"
                type="submit"
                showLoading={_showLoading}>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.PanelFooter>
          </Basic.Panel>
        </form>
      </div>
    );
  }
}

RoleSystemAttributeDetail.propTypes = {
  _showLoading: PropTypes.bool,
};
RoleSystemAttributeDetail.defaultProps = {
  _showLoading: false,
};

function select(state, component) {
  const entity = Utils.Entity.getEntity(state, roleSystemAttributeManager.getEntityType(), component.params.attributeId);
  let systemEntityHandling = null;
  if (entity) {
    entity.roleSystem = entity._embedded && entity._embedded.roleSystem ? entity._embedded.roleSystem.id : null;
    entity.schemaAttributeHandling = entity._embedded && entity._embedded.schemaAttributeHandling ? entity._embedded.schemaAttributeHandling.id : null;
    systemEntityHandling = entity._embedded && entity._embedded.roleSystem ? entity._embedded.roleSystem.systemEntityHandling.id : null;
  }
  return {
    _attribute: entity,
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`),
    _systemEntityHandlingId: systemEntityHandling
  };
}

export default connect(select)(RoleSystemAttributeDetail);
