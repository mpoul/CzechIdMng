import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import * as Basic from 'app/components/basic';
import * as Advanced from 'app/components/advanced';
import * as Utils from 'core/utils';
import uuid from 'uuid';
import { SecurityManager } from 'core/redux';

// Root key for tree
const rootKey = 'tree_root';

/**
* Table of organization
*/
export class OrganizationTable extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: true,
      detail: {
        show: false,
        entity: {}
      }
    };
  }

  getContentKey() {
    return 'content.organizations';
  }

  componentDidMount() {
    const { organizationManager } = this.props;
    const searchParameters = organizationManager.getService().getTreeSearchParameters();
    this.context.store.dispatch(organizationManager.fetchEntities(searchParameters, rootKey));
  }

  componentWillUnmount() {
    this.cancelFilter();
  }

  _reloadTree() {
    const { organizationManager } = this.props;
    const searchParameters = organizationManager.getService().getTreeSearchParameters();
    this.context.store.dispatch(organizationManager.fetchEntities(searchParameters, rootKey));
  }

  useFilter(event) {
    const { organizationManager } = this.props;

    if (event) {
      event.preventDefault();
    }
    const data = {
      ... this.refs.filterForm.getData(),
      parent: organizationManager.getSelfLink(this.refs.filterForm.getData().parent)
    };
    this.refs.table.getWrappedInstance().useFilterData(data);
  }

  _useFilterByTree(nodeId, event) {
    if (event) {
      event.preventDefault();
      // Stop propagation is important for prohibition of node tree expand.
      // After click on link node, we want only filtering ... not node expand.
      event.stopPropagation();
    }
    const { organizationManager } = this.props;
    if (!nodeId) {
      return;
    }
    const data = {
      ... this.refs.filterForm.getData(),
      parent: organizationManager.getSelfLink(nodeId)
    };
    this.refs.parent.setValue(nodeId);
    this.refs.table.getWrappedInstance().useFilterData(data);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  onDelete(bulkActionValue, selectedRows) {
    const { uiKey, organizationManager } = this.props;
    const selectedEntities = organizationManager.getEntitiesByIds(this.context.store.getState(), selectedRows);
    //
    this.refs['confirm-' + bulkActionValue].show(
      this.i18n(`action.${bulkActionValue}.message`, { count: selectedEntities.length, record: organizationManager.getNiceLabel(selectedEntities[0]), records: organizationManager.getNiceLabels(selectedEntities).join(', ') }),
      this.i18n(`action.${bulkActionValue}.header`, { count: selectedEntities.length, records: organizationManager.getNiceLabels(selectedEntities).join(', ') })
    ).then(() => {
      this.context.store.dispatch(organizationManager.deleteEntities(selectedEntities, uiKey, () => {
        this.refs.table.getWrappedInstance().reload();
      }));
    }, () => {
      // nothing
    });
  }

  /**
   * Recive new form for create new organization else show detail for existing org.
   */
  showDetail(entity, event) {
    if (event) {
      event.preventDefault();
    }
    if (entity.id === undefined) {
      const uuidId = uuid.v1();
      this.context.router.push(`/organizations/${uuidId}?new=1`);
    } else {
      this.context.router.push('/organizations/' + entity.id);
    }
  }

/**
 * Decorator for organization tree. Add custom icons and allow filtering after click on node
 */
  _orgTreeHeaderDecorator(props) {
    const style = props.style;
    const iconType = props.node.isLeaf ? 'group' : 'building';
    const iconClass = `fa fa-${iconType}`;
    const iconStyle = { marginRight: '5px' };
    return (
      <div style={style.base}>
        <div style={style.title}>
          <i className={iconClass} style={iconStyle}/>
          <Basic.Button level="link" onClick={this._useFilterByTree.bind(this, props.node.id)} style={{padding: '0px 0px 0px 0px'}}>
            {props.node.name}
          </Basic.Button>

        </div>
      </div>
    );
  }

  render() {
    const { uiKey, organizationManager, _root, _showLoading } = this.props;
    const { filterOpened } = this.state;

    return (
      <Basic.Row>
        <div className="col-lg-12">
          <div className="col-lg-3">
            <Basic.Panel style={{ marginTop: 15 }}>
              {
              !_root
              ||
              <Advanced.Tree
                ref="organizationTree"
                rootNode={{name: _root.name, toggled: true, id: _root.id}}
                propertyId="id"
                propertyParent="parent"
                showLoading={_showLoading}
                propertyName="name"
                headerDecorator={this._orgTreeHeaderDecorator.bind(this)}
                uiKey="orgTree"
                manager={organizationManager}
                />
              }
            </Basic.Panel>
          </div>
          <div className="col-lg-9" style={{ paddingRight: 0, paddingLeft: 0 }}>
            <Basic.Confirm ref="confirm-delete" level="danger"/>
            <Advanced.Table
              ref="table"
              uiKey={uiKey}
              manager={organizationManager}
              showRowSelection={SecurityManager.hasAuthority('ORGANIZATION_DELETE')}
              rowClass={({rowIndex, data}) => { return data[rowIndex].disabled ? 'disabled' : ''; }}
              style={{ borderLeft: '1px solid #ddd' }}
              filter={
                <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
                  <Basic.AbstractForm ref="filterForm" className="form-horizontal">
                    <Basic.Row>
                      <div className="col-lg-6">
                        <Advanced.Filter.TextField
                          ref="text"
                          placeholder={this.i18n('entity.Organization.name')}
                          label={this.i18n('entity.Organization.name')}/>
                      </div>
                      <div className="col-lg-6 text-right">
                        <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                      </div>
                    </Basic.Row>
                    <Basic.Row className="last">
                      <div className="col-lg-6">
                        {
                        <Advanced.Filter.SelectBox
                          ref="parent"
                          placeholder={this.i18n('entity.Organization.parentId')}
                          label={this.i18n('filter.parentId.label')}
                          manager={organizationManager}/>
                          }
                      </div>
                      <div className="col-lg-6">
                      </div>
                    </Basic.Row>
                  </Basic.AbstractForm>
                </Advanced.Filter>
              }
              filterOpened={!filterOpened}
              actions={
                [
                  { value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }
                ]
             }
              buttons={
                [
                  <Basic.Button level="success" key="add_button" className="btn-xs" onClick={this.showDetail.bind(this, {})} rendered={SecurityManager.hasAuthority('ORGANIZATION_WRITE')}>
                    <Basic.Icon type="fa" icon="plus"/>
                    {' '}
                    {this.i18n('button.add')}
                  </Basic.Button>
                ]
              }>
              <Advanced.Column
                header=""
                className="detail-button"
                cell={
                  ({ rowIndex, data }) => {
                    return (
                      <Advanced.DetailButton
                        title={this.i18n('button.detail')}
                        onClick={this.showDetail.bind(this, data[rowIndex])}/>
                    );
                  }
                }
                sort={false}/>
              <Advanced.Column property="name" sort/>
              <Advanced.Column property="parent.name" sort/>
              <Advanced.Column property="disabled" sort face="bool"/>
              <Advanced.Column property="shortName" sort rendered={false}/>
              <Advanced.Column property="parentId" sort rendered={false}/>
            </Advanced.Table>
          </div>
        </div>
      </Basic.Row>
    );
  }
}

OrganizationTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  filterOpened: PropTypes.bool,
  organizationManager: PropTypes.object.isRequired,
  organizationTree: PropTypes.object.isRequired
};

OrganizationTable.defaultProps = {
  _showLoading: false
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey),
    _showLoading: Utils.Ui.isShowLoading(state, `${component.uiKey}-detail`),
    _root: Utils.Ui.getEntities(state, rootKey)[0]
  };
}

export default connect(select, null, null, { withRef: true })(OrganizationTable);
