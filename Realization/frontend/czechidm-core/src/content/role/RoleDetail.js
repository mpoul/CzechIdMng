import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import _ from 'lodash';
import { connect } from 'react-redux';
import * as Utils from '../../utils';
import RoleTypeEnum from '../../enums/RoleTypeEnum';
import authorityHelp from './AuthoritiesPanel_cs.md';
import AuthoritiesPanel from './AuthoritiesPanel';
import * as Basic from '../../components/basic';
import { RoleManager, WorkflowProcessDefinitionManager, SecurityManager, IdentityManager, RoleCatalogueManager } from '../../redux';

const workflowProcessDefinitionManager = new WorkflowProcessDefinitionManager();
const roleManager = new RoleManager();
const identityManger = new IdentityManager();
const roleCatalogueManager = new RoleCatalogueManager();

class RoleDetail extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.state = {
      _showLoading: true
    };
  }

  getContentKey() {
    return 'content.roles';
  }

  componentDidMount() {
    const { entity } = this.props;

    if (Utils.Entity.isNew(entity)) {
      this._setSelectedEntity(entity);
    } else {
      this._setSelectedEntity(this._prepareEntity(entity));
    }
  }

  componentWillReceiveProps(nextProps) {
    const { entity } = this.props;
    if (nextProps.entity && nextProps.entity !== entity && nextProps.entity.subRoles) {
      this._setSelectedEntity(this._prepareEntity(nextProps.entity));
    }
  }

  _prepareEntity(entity) {
    const copyOfEntity = _.merge({}, entity); // we can not modify given entity
    // we dont need to load entities again - we have them in embedded objects
    copyOfEntity.subRoles = !entity.subRoles ? [] : entity.subRoles.map(subRole => { return subRole._embedded.sub; });
    copyOfEntity.superiorRoles = !entity.superiorRoles ? [] : entity.superiorRoles.map(superiorRole => { return superiorRole._embedded.superior; });
    copyOfEntity.guarantees = !entity.guarantees ? [] : entity.guarantees.map(guarantee => { return guarantee._embedded.guarantee; });
    if (copyOfEntity._embedded !== undefined && copyOfEntity._embedded.roleCatalogue !== undefined) {
      copyOfEntity.roleCatalogue = copyOfEntity._embedded.roleCatalogue.id;
    }
    return copyOfEntity;
  }

  _setSelectedEntity(entity) {
    this.setState({
      _showLoading: false
    }, () => {
      this.refs.form.setData(entity);
      this.refs.name.focus();
    });
  }

  save(afterAction, event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    this.setState({
      _showLoading: true
    }, () => {
      const entity = this.refs.form.getData();
      // append selected authorities
      entity.authorities = this.refs.authorities.getWrappedInstance().getSelectedAuthorities();
      // append subroles
      if (entity.subRoles) {
        entity.subRoles = entity.subRoles.map(subRoleId => {
          return {
            sub: roleManager.getSelfLink(subRoleId)
          };
        });
      }
      if (entity.guarantees) {
        entity.guarantees = entity.guarantees.map(guaranteeId => {
          return {
            guarantee: identityManger.getSelfLink(guaranteeId)
          };
        });
      }
      // transform object roleCatalogue to self link
      if (entity.roleCatalogue) {
        entity.roleCatalogue = roleCatalogueManager.getSelfLink(entity.roleCatalogue);
      }
      // delete superior roles - we dont want to save them (they are ignored on BE anyway)
      delete entity.superiorRoles;
      //
      this.getLogger().debug('[RoleDetail] save entity', entity);
      if (Utils.Entity.isNew(entity)) {
        this.context.store.dispatch(roleManager.createEntity(entity, null, (createdEntity, error) => {
          this._afterSave(createdEntity, error, afterAction);
        }));
      } else {
        this.context.store.dispatch(roleManager.patchEntity(entity, null, (patchedEntity, error) => {
          this._afterSave(patchedEntity, error, afterAction);
        }));
      }
    });
  }

  _afterSave(entity, error, afterAction = 'CLOSE') {
    if (error) {
      this.refs.form.processEnded();
      this.addError(error);
      return;
    }
    this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
    if (afterAction === 'CLOSE') {
      this.context.router.replace(`roles`);
    } else {
      this.context.router.replace(`role/${entity.id}/detail`);
    }
  }

  render() {
    const { entity, showLoading } = this.props;
    const { _showLoading } = this.state;
    return (
      <div>
        <Helmet title={Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('edit.title')} />

        <form onSubmit={this.save.bind(this)}>
          <Basic.Panel className={Utils.Entity.isNew(entity) ? '' : 'no-border last'}>
            <Basic.PanelHeader text={Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('tabs.basic')} />

            <Basic.PanelBody style={Utils.Entity.isNew(entity) ? { paddingTop: 0, paddingBottom: 0 } : { padding: 0 }}>
              <Basic.AbstractForm ref="form" showLoading={ _showLoading || showLoading } readOnly={!SecurityManager.hasAuthority('ROLE_WRITE')}>
                <Basic.Row>
                  <div className="col-lg-8">
                    <h3 style={{ margin: '0 0 10px 0', padding: 0, borderBottom: '1px solid #ddd' }}>{this.i18n('setting.basic.header')}</h3>
                    <div className="form-horizontal">
                      <Basic.TextField
                        ref="name"
                        label={this.i18n('entity.Role.name')}
                        required
                        min={0}
                        max={255}/>
                      <Basic.EnumSelectBox
                        ref="roleType"
                        label={this.i18n('entity.Role.roleType')}
                        enum={RoleTypeEnum}
                        required
                        readOnly={!Utils.Entity.isNew(entity)}/>
                      <Basic.SelectBox
                        ref="roleCatalogue"
                        label={this.i18n('entity.Role.roleCatalogue.name')}
                        manager={roleCatalogueManager}/>
                      <Basic.SelectBox
                        ref="superiorRoles"
                        label={this.i18n('entity.Role.superiorRoles')}
                        manager={roleManager}
                        multiSelect
                        readOnly
                        placeholder=""/>
                      <Basic.SelectBox
                        ref="subRoles"
                        label={this.i18n('entity.Role.subRoles')}
                        manager={roleManager}
                        multiSelect/>
                      <Basic.SelectBox
                        ref="guarantees"
                        label={this.i18n('entity.Role.guarantees')}
                        multiSelect
                        manager={identityManger}/>
                      <Basic.TextArea
                        ref="description"
                        label={this.i18n('entity.Role.description')}
                        max={255}/>
                      <Basic.Checkbox
                        ref="disabled"
                        label={this.i18n('entity.Role.disabled')}/>
                    </div>

                    <h3 style={{ margin: '20px 0 10px 0', padding: 0, borderBottom: '1px solid #ddd' }}>
                      { this.i18n('setting.approval.header') }
                    </h3>
                    <Basic.SelectBox
                      labelSpan=""
                      componentSpan=""
                      ref="approveAddWorkflow"
                      label={this.i18n('entity.Role.approveAddWorkflow')}
                      forceSearchParameters={ workflowProcessDefinitionManager.getDefaultSearchParameters().setFilter('category', 'eu.bcvsolutions.role.approve.add') }
                      multiSelect={false}
                      manager={workflowProcessDefinitionManager}/>
                    <Basic.SelectBox
                      labelSpan=""
                      componentSpan=""
                      ref="approveRemoveWorkflow"
                      label={this.i18n('entity.Role.approveRemoveWorkflow')}
                      forceSearchParameters={ workflowProcessDefinitionManager.getDefaultSearchParameters().setFilter('category', 'eu.bcvsolutions.role.approve.remove') }
                      multiSelect={false}
                      manager={workflowProcessDefinitionManager}/>
                  </div>

                  <div className="col-lg-4">
                    <h3 style={{ margin: '0 0 10px 0', padding: 0, borderBottom: '1px solid #ddd' }}>
                      <span dangerouslySetInnerHTML={{ __html: this.i18n('setting.authority.header') }} className="pull-left"/>
                      <Basic.HelpIcon content={authorityHelp} className="pull-right"/>
                      <div className="clearfix"/>
                    </h3>
                    <AuthoritiesPanel
                      ref="authorities"
                      roleManager={roleManager}
                      authorities={entity.authorities}
                      disabled={!SecurityManager.hasAuthority('ROLE_WRITE')}/>
                  </div>
                </Basic.Row>
              </Basic.AbstractForm>
            </Basic.PanelBody>

            <Basic.PanelFooter>
              <Basic.Button type="button" level="link" onClick={this.context.router.goBack} showLoading={_showLoading}>{this.i18n('button.back')}</Basic.Button>

              <Basic.SplitButton
                level="success"
                title={this.i18n('button.saveAndContinue')}
                onClick={this.save.bind(this, 'CONTINUE')}
                showLoading={_showLoading}
                showLoadingIcon
                showLoadingText={this.i18n('button.saving')}
                rendered={SecurityManager.hasAuthority('ROLE_WRITE')}
                pullRight
                dropup>
                <Basic.MenuItem eventKey="1" onClick={this.save.bind(this, 'CLOSE')}>{this.i18n('button.saveAndClose')}</Basic.MenuItem>
              </Basic.SplitButton>
            </Basic.PanelFooter>
          </Basic.Panel>
          {/* onEnter action - is needed because SplitButton is used instead standard submit button */}
          <input type="submit" className="hidden"/>
        </form>
      </div>
    );
  }
}


RoleDetail.propTypes = {
  entity: PropTypes.object,
  showLoading: PropTypes.bool
};
RoleDetail.defaultProps = {
};

export default connect()(RoleDetail);
