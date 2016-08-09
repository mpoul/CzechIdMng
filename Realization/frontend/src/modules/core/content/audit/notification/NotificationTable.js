import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import * as Basic from 'app/components/basic';
import * as Advanced from 'app/components/advanced';
import * as Utils from 'core/utils';
import NotificationRecipient from './NotificationRecipient';
import { IdentityManager } from 'core/redux';
import NotificationStateEnum from 'core/enums/NotificationStateEnum';

/**
* Table of roles
*/
export class NotificationTable extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: this.props.filterOpened,
      detail: {
        show: false,
        entity: {}
      }
    };
    this.identityManager = new IdentityManager();
  }

  getContentKey() {
    return 'content.notifications';
  }

  componentDidMount() {
  }

  componentDidUpdate() {
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().useFilter(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  showDetail(entity, event) {
    if (event) {
      event.preventDefault();
    }
    this.context.router.push('/audit/notification/' + entity.id);
  }

  render() {
    const { uiKey, notificationManager } = this.props;
    const { filterOpened } = this.state;

    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          manager={notificationManager}
          rowClass={({rowIndex, data}) => { return Utils.Ui.getRowClass(data[rowIndex]); }}
          filterOpened={filterOpened}
          filter={
            <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
              <Basic.AbstractForm ref="filterForm" className="form-horizontal">
                <Basic.Row>
                  <div className="col-lg-4">
                    <Advanced.Filter.DateTimePicker
                      mode="date"
                      ref="createdFrom"
                      placeholder={this.i18n('filter.dateFrom.placeholder')}
                      label={this.i18n('filter.dateFrom.label')}/>
                  </div>
                  <div className="col-lg-4">
                    <Advanced.Filter.DateTimePicker
                      mode="date"
                      ref="createdTill"
                      placeholder={this.i18n('filter.dateTill.placeholder')}
                      label={this.i18n('filter.dateTill.label')}/>
                  </div>
                  <div className="col-lg-4 text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                  </div>
                </Basic.Row>

                <Basic.Row>
                  <div className="col-lg-4">
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={this.i18n('filter.text.placeholder')}
                      label={this.i18n('filter.text.label')}/>
                  </div>
                  <div className="col-lg-4">
                    <Advanced.Filter.SelectBox
                      ref="sender"
                      label={this.i18n('filter.sender.label')}
                      placeholder={this.i18n('filter.sender.placeholder')}
                      multiSelect={false}
                      manager={this.identityManager}
                      returnProperty="username"/>
                  </div>
                  <div className="col-lg-4">
                    <Advanced.Filter.SelectBox
                      ref="recipient"
                      label={this.i18n('filter.recipient.label')}
                      placeholder={this.i18n('filter.recipient.placeholder')}
                      multiSelect={false}
                      manager={this.identityManager}
                      returnProperty="username"/>
                  </div>
                </Basic.Row>

                <Basic.Row className="last">
                  <div className="col-lg-4">
                    <Advanced.Filter.EnumSelectBox
                      ref="sent"
                      label={this.i18n('filter.sent.label')}
                      placeholder={this.i18n('filter.sent.placeholder')}
                      enum={NotificationStateEnum}/>
                  </div>
                  <div className="col-lg-4">
                  </div>
                  <div className="col-lg-4">
                  </div>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          >

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
            }/>
          <Advanced.Column property="created" sort face="datetime"/>
          <Advanced.Column property="topic" sort face="text"/>
          <Advanced.Column property="message.subject" sort face="text"/>
          <Advanced.Column
            property="recipients"
            cell={
              ({ rowIndex, data, property }) => {
                return data[rowIndex][property].map(recipient => {
                  return (
                    <NotificationRecipient recipient={recipient} identityOnly />
                  );
                });
              }
            }/>
          <Advanced.Column
            property="from"
            cell={
              ({ rowIndex, data, property }) => {
                return (
                  <NotificationRecipient recipient={data[rowIndex][property]} identityOnly />
                );
              }
            }/>
          <Advanced.Column
            property="sent"
            cell={
              ({ rowIndex, data, property }) => {
                const sentCount = data[rowIndex].relatedNotifications.reduce((result, notification) => { return result + (notification.sent ? 1 : 0); }, 0);
                if (sentCount === data[rowIndex].relatedNotifications.length) {
                  return (
                    <Advanced.DateValue value={data[rowIndex][property]}/>
                  );
                }
                if (sentCount === 0) {
                  return (
                    <Basic.Label level="danger" text={NotificationStateEnum.getNiceLabelBySymbol(NotificationStateEnum.NOT)}/>
                  );
                }
                return (
                  <Basic.Label level="warning" text={NotificationStateEnum.getNiceLabelBySymbol(NotificationStateEnum.PARTLY)}/>
                );
              }
            }/>
          <Advanced.Column property="sentLog" sort face="text" rendered={false}/>
        </Advanced.Table>
      </div>
    );
  }
}

NotificationTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  notificationManager: PropTypes.object.isRequired,
  filterOpened: PropTypes.bool
};

NotificationTable.defaultProps = {
  filterOpened: false,
  _showLoading: false
};

function select(state, component) {
  return {
    _searchParameters: state.data.ui[component.uiKey] ? state.data.ui[component.uiKey].searchParameters : {},
    _showLoading: component.notificationManager.isShowLoading(state, `${component.uiKey}-detail`)
  };
}

export default connect(select, null, null, { withRef: true })(NotificationTable);