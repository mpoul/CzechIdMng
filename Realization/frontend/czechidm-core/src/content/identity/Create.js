import React from 'react';
import _ from 'lodash';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import Joi from 'joi';
import Immutable from 'immutable';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { AuthenticateService } from '../../services';
import { IdentityManager } from '../../redux';

const identityManager = new IdentityManager();

class Profile extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.state = {
      detail: {
        entity: null
      },
      showLoading: false,
      generatePassword: true,
      generatePasswordShowLoading: false
    };
  }

 initData() {
   // TODO: load data from redux store
   const { generatePassword } = this.state;
   this.transformData({
     generatePassword
   });
   this.refs.username.focus();
   if (generatePassword) {
     this.generatePassword();
   }
 }

  componentDidMount() {
    this.selectNavigationItem('identities');
    // TODO: load data from redux store
    this.initData();
  }

  componentDidUpdate() {
    // nothing
  }

  save(editContinue = 'CLOSE', event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const formData = this.refs.form.getData();

    // add data from child component to formData
    formData.password = this.refs.passwords.getValue();

    if (!this.refs.passwords.validate()) {
      return;
    }

    this.setState({
      showLoading: true
    }, this.refs.form.processStarted());

    const result = _.merge({}, formData, {
      password: btoa(formData.password) // base64
    });
    delete result.passwordAgain;
    delete result.generatePassword;
    // TODO: transform to redux
    identityManager.getService().create(result)
    .then(json => {
      this.setState({
        showLoading: false
      }, () => {
        this.refs.form.processEnded();
        this.addMessage({
          message: this.i18n('content.identity.create.message.success', { username: json.username})
        });
        switch (editContinue) {
          case 'EDIT': {
            this.context.router.replace(`identity/${json.username}/profile`);
            break;
          }
          case 'NEW': {
            this.initData();
            this.context.router.replace(`identity/new`);
            break;
          }
          default : {
            this.context.router.push(`identities`);
          }
        }
      });
    }).catch(error => {
      this.setState({
        showLoading: false
      }, () => {
        this.refs.form.processEnded();
        this.transformData(null);
        this.addError(error);
      });
    });
  }

  transformData(json) {
    let result;
    if (json) {
      result = _.merge({}, json, { email: '' });
    }
    this.setState({
      detail: {
        entity: result
      }
    });
  }

  setNewPassword(password) {
    const formData = this.refs.form.getData();
    _.merge(formData, {
      generatePassword: true
    });
    this.setState({
      detail: {
        entity: formData,
      },
      password,
      passwordAgain: password
    });
  }

  generatePassword(event = null) {
    const generate = event ? event.currentTarget.checked : true;
    this.setState({
      generatePassword: generate
    });
    if (!generate) {
      return;
    }
    /*
    // generate
    this.setState({
      generatePasswordShowLoading: true
    })
    identityManager.getService().generatePassword()
    .then(response => {
      return response.json();
    })
    .then(json => {
      if (!json.error) {
        this.setNewPassword(json.password);
        this.setState({
          generatePasswordShowLoading: false,
          generatePassword: true
        });
      } else {
        this.addError(json.error);
        this.setState({
          generatePasswordShowLoading: false,
          generatePassword: false
        });
      }
    })
    .catch(error => {
      this.addError(error);
      this.setState({
        generatePasswordShowLoading: false,
        generatePassword: false
      });
    });*/
    // TODO: rest service for password generate
    this.setNewPassword(AuthenticateService.generatePassword());
  }

  canEditMap() {
    let canEditMap = new Immutable.Map();
    canEditMap = canEditMap.set('isSaveEnabled', true);
    return canEditMap;
  }

  render() {
    const { detail, showLoading, generatePassword, generatePasswordShowLoading, password, passwordAgain } = this.state;

    return (
      <Basic.Row>
        <div className="col-lg-offset-1 col-lg-10">
          <Helmet title={this.i18n('content.identity.create.title')} />
          <form onSubmit={this.save.bind(this, 'CLOSE')}>
            <Basic.Panel>
              <Basic.PanelHeader text={this.i18n('content.identity.create.header')}/>

              <Basic.PanelBody style={{ paddingTop: 0, paddingBottom: 0 }}>
                <Basic.AbstractForm ref="form" data={detail.entity} className="form-horizontal">
                  <div className="col-lg-7">
                    <Basic.TextField ref="username" label={this.i18n('content.identity.profile.username')} required min={3} max={30}/>
                    <Basic.TextField ref="lastName" label={this.i18n('content.identity.profile.lastName')} required max={255}/>
                    <Basic.TextField ref="firstName" label={this.i18n('content.identity.profile.firstName')} max={255}/>
                    <Basic.TextField ref="titleBefore" label={this.i18n('entity.Identity.titleBefore')} max={100}/>
                    <Basic.TextField ref="titleAfter" label={this.i18n('entity.Identity.titleAfter')} max={100}/>
                    <Basic.TextField ref="email"
                      label={this.i18n('content.identity.profile.email.label')}
                      placeholder={this.i18n('content.identity.profile.email.placeholder')}
                      validation={Joi.string().email()}/>
                    <Basic.Checkbox ref="disabled" label={this.i18n('entity.Identity.disabled')}/>
                    <Basic.TextArea ref="description"
                      label={this.i18n('content.identity.profile.description.label')}
                      placeholder={this.i18n('content.identity.profile.description.placeholder')}
                      rows={4}
                      max={255}/>
                  </div>
                  <div className="col-lg-5">
                    <Basic.Checkbox ref="generatePassword" value={generatePassword} label={this.i18n('content.identity.create.button.generate')} onChange={this.generatePassword.bind(this)}/>

                    <Advanced.PasswordField
                      className="form-control"
                      ref="passwords"
                      type={generatePassword || generatePasswordShowLoading ? 'text' : 'password'}
                      required={!generatePassword}
                      readOnly={generatePassword}
                      newPassword={password}
                      newPasswordAgain={passwordAgain}/>

                  </div>
                </Basic.AbstractForm>
              </Basic.PanelBody>

              <Basic.PanelFooter>
                <Basic.Button type="button" level="link" onClick={this.context.router.goBack} showLoading={showLoading}>{this.i18n('button.back')}</Basic.Button>

                <Basic.SplitButton level="success" title={this.i18n('button.create')} onClick={this.save.bind(this, 'CLOSE')} showLoading={showLoading} pullRight dropup>
                  <Basic.MenuItem eventKey="1" onClick={this.save.bind(this, 'EDIT')}>{this.i18n('button.createContinue')}</Basic.MenuItem>
                  <Basic.MenuItem eventKey="1" onClick={this.save.bind(this, 'NEW')}>{this.i18n('button.createNew')}</Basic.MenuItem>
                </Basic.SplitButton>

              </Basic.PanelFooter>
            </Basic.Panel>
            {/* onEnter action - is needed because SplitButton is used instead standard submit button */}
            <input type="submit" className="hidden"/>
          </form>
        </div>
      </Basic.Row>
    );
  }
}

Profile.propTypes = {
  userContext: React.PropTypes.object
};
Profile.defaultProps = {
  userContext: null
};

function select(state) {
  return {
    userContext: state.security.userContext
  };
}

export default connect(select)(Profile);
