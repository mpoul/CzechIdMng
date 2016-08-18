import React, { PropTypes } from 'react';
import * as Basic from 'app/components/basic';
import { OrganizationManager } from 'core/redux';

/**
 * Organization detail content
 */
export default class OrganizationDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.organizationManager = new OrganizationManager();
  }

  getContentKey() {
    return 'content.organizations';
  }

  componentDidMount() {
    const { organization } = this.props;
    this.selectNavigationItem('organizations');

    if (organization !== undefined) {
      const loadedOrganization = organization;
      if (organization._embedded) {
        loadedOrganization.parent = organization._embedded.parent.id;
      }
      this.refs.form.setData(loadedOrganization);
    }
  }

  save(event) {
    const { uiKey } = this.props;

    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const entity = this.refs.form.getData();

    if (entity.parent) {
      entity.parent = this.organizationManager.getSelfLink(entity.parent);
    }

    if (entity.id === undefined) {
      this.context.store.dispatch(this.organizationManager.createEntity(entity, `${uiKey}-detail`, (createdEntity, error) => {
        this._afterSave(createdEntity, error);
        if (!error) {
          this.refs.table.getWrappedInstance().reload();
        }
      }));
    } else {
      this.context.store.dispatch(this.organizationManager.patchEntity(entity, `${uiKey}-detail`, this._afterSave.bind(this)));
    }
  }

  _afterSave(entity, error) {
    if (error) {
      this.refs.form.processEnded();
      this.addError(error);
      return;
    }
    this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
    this.closeDetail();
    this.context.router.replace(`organizations/`);
  }

  closeDetail() {
    this.setState({
      detail: {
        show: false,
        entity: {}
      }
    });
  }

  render() {
    const { uiKey, organization } = this.props;
    return (
      <div>
        <form onSubmit={this.save.bind(this)}>
            <Basic.AbstractForm ref="form" uiKey={uiKey} className="form-horizontal" >
              <Basic.TextField
                ref="name"
                label={this.i18n('entity.Organization.name')}
                required/>
              <Basic.Checkbox
                ref="disabled"
                label={this.i18n('entity.Organization.disabled')}/>
                <Basic.SelectBox
                  ref="parent"
                  label={this.i18n('entity.Role.subRoles')}
                  manager={this.organizationManager}/>
            </Basic.AbstractForm>

            <Basic.PanelFooter >
              <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
                <Basic.Button
                  type="submit"
                  level="success"
                  showLoadingIcon
                  showLoadingText={this.i18n('button.saving')}>
                  {this.i18n('button.save')}
                </Basic.Button>
            </Basic.PanelFooter>
          </form>
      </div>
    );
  }
}

OrganizationDetail.propTypes = {
  organization: PropTypes.object,
  uiKey: PropTypes.string.isRequired,
};
OrganizationDetail.defaultProps = {
};