import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import { Basic, Utils} from 'czechidm-core';
import { SystemEntityHandlingManager, SchemaAttributeHandlingManager, SchemaAttributeManager} from '../../redux';

const uiKey = 'schema-attribute-handling';
const manager = new SchemaAttributeHandlingManager();
const systemEntityHandlingManager = new SystemEntityHandlingManager();
const schemaAttributeManager = new SchemaAttributeManager();

class SchemaAttributeHandling extends Basic.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
  }

  getManager() {
    return manager;
  }

  getUiKey() {
    return uiKey;
  }

  getContentKey() {
    return 'acc:content.schema.attribute-handling';
  }

  componentWillReceiveProps(nextProps) {
    const { entityId} = nextProps.params;
    if (entityId && entityId !== this.props.params.entityId) {
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
    const { entityId} = props.params;
    if (this._getIsNew(props)) {
      this.setState({attribute: {systemEntityHandling: props.location.query.entityHandlingId}});
    } else {
      this.context.store.dispatch(this.getManager().fetchEntity(entityId));
    }
    this.selectNavigationItems(['sys-systems']);
  }

  _getIsNew(nextProps) {
    const { query } = nextProps ? nextProps.location : this.props.location;
    return (query) ? query.new : null;
  }

  save(entity, event) {
    const formEntity = this.refs.form.getData();
    formEntity.systemEntityHandling = systemEntityHandlingManager.getSelfLink(formEntity.systemEntityHandling);
    formEntity.schemaAttribute = schemaAttributeManager.getSelfLink(formEntity.schemaAttribute);
    //
    super.save(formEntity, event);
  }

  afterSave(entity, error) {
    if (!error) {
      if (this._getIsNew()) {
        this.addMessage({ message: this.i18n('create.success', { name: entity.idmPropertyName }) });
        this.context.router.replace(`/schema-attributes-handling/${entity.id}/detail`, {entityId: entity.id});
      } else {
        this.addMessage({ message: this.i18n('save.success', { name: entity.idmPropertyName }) });
      }
    }
    super.afterSave();
  }

  closeDetail() {
    this.refs.form.processEnded();
  }

  render() {
    const { _showLoading, _attribute} = this.props;
    const isNew = this._getIsNew();
    const attribute = isNew ? this.state.attribute : _attribute;
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.ContentHeader>
          <Basic.Icon value="list-alt"/>
          {' '}
          <span dangerouslySetInnerHTML={{ __html: this.i18n('header', attribute ? { name: attribute.idmPropertyName} : {})}}/>
        </Basic.ContentHeader>

        <Basic.Panel>
          <Basic.AbstractForm ref="form" data={attribute} showLoading={_showLoading} className="form-horizontal">
            <Basic.SelectBox
              ref="systemEntityHandling"
              manager={systemEntityHandlingManager}
              label={this.i18n('acc:entity.SchemaAttributeHandling.systemEntityHandling')}
              readOnly
              required/>
            <Basic.TextField
              ref="idmPropertyName"
              label={this.i18n('acc:entity.SchemaAttributeHandling.idmPropertyName')}
              required
              max={255}/>
            <Basic.SelectBox
              ref="schemaAttribute"
              manager={schemaAttributeManager}
              label={this.i18n('acc:entity.SchemaAttributeHandling.schemaAttribute')}
              required/>
            <Basic.Checkbox
              ref="extendedAttribute"
              label={this.i18n('acc:entity.SchemaAttributeHandling.extendedAttribute')}/>
            <Basic.TextField
              ref="transformFromSystem"
              label={this.i18n('acc:entity.SchemaAttributeHandling.transformFromSystem')}
              max={255}/>
            <Basic.TextField
              ref="transformToSystem"
              label={this.i18n('acc:entity.SchemaAttributeHandling.transformToSystem')}
              max={255}/>
          </Basic.AbstractForm>
          <Basic.PanelFooter>
            <Basic.Button type="button" level="link"
              onClick={this.context.router.goBack}
              showLoading={_showLoading}>
              {this.i18n('button.back')}
            </Basic.Button>
            <Basic.Button
              onClick={this.save.bind(this)}
              level="success" showLoading={_showLoading}>
              {this.i18n('button.save')}
            </Basic.Button>
          </Basic.PanelFooter>
        </Basic.Panel>
      </div>
    );
  }
}

SchemaAttributeHandling.propTypes = {
  system: PropTypes.object,
  _showLoading: PropTypes.bool,
};
SchemaAttributeHandling.defaultProps = {
  system: null,
  _showLoading: false,
};

function select(state, component) {
  const entity = Utils.Entity.getEntity(state, manager.getEntityType(), component.params.entityId);
  if (entity) {
    const systemEntityHandling = entity._embedded && entity._embedded.systemEntityHandling ? entity._embedded.systemEntityHandling.id : null;
    const schemaAttribute = entity._embedded && entity._embedded.schemaAttribute ? entity._embedded.schemaAttribute.id : null;
    entity.systemEntityHandling = systemEntityHandling;
    entity.schemaAttribute = schemaAttribute;
  }
  return {
    _attribute: entity,
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`),
  };
}

export default connect(select)(SchemaAttributeHandling);