import * as Utils from '../utils';
import AbstractService from './AbstractService';
import RestApiService from './RestApiService';
import SearchParameters from '../domain/SearchParameters';

class IdentityService extends AbstractService {

  getApiPath() {
    return '/identities';
  }

  getNiceLabel(entity) {
    let toString = '';
    if (entity) {
      toString += this.getFullName(entity);
      toString += (entity.username ? ` (${entity.username})` : '');
    }
    return toString;
  }

  getFullName(entity) {
    let toString = '';
    if (entity) {
      toString += (entity.titleBefore ? (entity.titleBefore + ' ') : '');
      toString += (entity.firstName ? (entity.firstName + ' ') : '');
      toString += (entity.lastName ? entity.lastName : '');
      toString += (entity.titleAfter ? `, ${entity.titleAfter}` : '');
    }
    return toString;
  }

  /**
   * Change users pasword in given systems (by json)
   *
   * @param username {string}
   * @param passwordChangeDto {object}
   * @param token {string} - if token has to be used (from public page is is not needed, then *false* could be given)
   * @return {Promise}
   */
  passwordChange(username, passwordChangeDto, token = null) {
    return RestApiService.put(RestApiService.getUrl(`/public${this.getApiPath()}/${username}/password-change`), passwordChangeDto, token);
  }

  /**
   * Resets users pasword in given systems (by json)
   *
   * @param username {string}
   * @param passwordResetDto {object}
   * @return {Promise}
   */
  passwordReset(username, passwordResetDto) {
    return RestApiService.put(this.getApiPath() + `/${username}/password/reset`, passwordResetDto);
  }


  /**
   * Generates password based on configured idm policies
   *
   * @return {Promise}
   */
  generatePassword() {
    return RestApiService.post(this.getApiPath() + `/password/generate`);
  }

  /**
   * Generates token, whitch is required for reset given user's password
   *
   * @param username {string}
   * @return {Promise}
   */
  generatePasswordResetToken(username) {
    return RestApiService.post(this.getApiPath() + `/password/reset/token?name=${username}`);
  }

  /**
   * Resets password by given token. Token is generated for specific username.
   * @return {Promise}
   */
  paswordResetByToken(token, password) {
    return RestApiService.put(this.getApiPath() + `/password/reset/token`, {
      token,
      password
    });
  }

  /**
   * Required idm password change for given user without login
   *
   * @param username {string}
   * @param oldPassword {string}
   * @param newPassword {string}
   * @return {Promise}
   */
  passwordMustChange(username, oldPassword, newPassword) {
    return RestApiService.put(this.getApiPath() + `/password/must-change`, {
      identity: username,
      oldPassword,
      newPassword
    });
  }

  /**
   * Get given user's roles
   *
   * @param username {string}
   * @param token {string}
   * @return {Promise}
   */
  getRoles(username, token = null) {
    return RestApiService
    .get(this.getApiPath() + `/${username}/roles`, token)
    .then(response => {
      return response.json();
    })
    .then(json => {
      if (Utils.Response.hasError(json)) {
        throw Utils.Response.getFirstError(json);
      }
      return json;
    });
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('username');
  }

  /**
   * Deactivates given identity
   *
   * @param  {String} identity
   * @return {Promise}
   */
  deactivate(username) {
    return this.patchById(username, { disabled: true });
  }

  /**
   * Activates given identity
   *
   * @param  {String} identity
   * @return {Promise}
   */
  activate(username) {
    return this.patchById(username, { disabled: false });
  }

  /**
   * Returns identity accounts
   *
   * @param  {String} username
   * @return {Promise}
   */
  getAccounts(username) {
    return RestApiService.get(this.getApiPath() + `/${username}/accounts`);
  }

  /**
   * Get given identity's contracts
   *
   * @param username {string}
   * @param token {string}
   * @return {Promise}
   */
  getContracts(username) {
    return RestApiService
    .get(this.getApiPath() + `/${username}/identity-contracts`)
    .then(response => {
      return response.json();
    })
    .then(json => {
      if (Utils.Response.hasError(json)) {
        throw Utils.Response.getFirstError(json);
      }
      return json;
    });
  }

  getAuthorities(username) {
    return RestApiService
    .get(this.getApiPath() + `/${username}/authorities`)
    .then(response => {
      return response.json();
    })
    .then(json => {
      if (Utils.Response.hasError(json)) {
        throw Utils.Response.getFirstError(json);
      }
      return json;
    });
  }

  /**
   * Start workflow for change permissions
   * @param  id Identity id
   * @return Promise  task instance
   */
  changePermissions(id) {
    return RestApiService.put(this.getApiPath() + `/${id}/change-permissions`, null).then(response => {
      if (response.status === 403) {
        throw new Error(403);
      }
      if (response.status === 404) {
        throw new Error(404);
      }
      return response.json();
    })
    .then(json => {
      if (Utils.Response.hasError(json)) {
        throw Utils.Response.getFirstError(json);
      }
      return json;
    });
  }

  /**
   * Returns form definition to given identity
	 *
   * @param  {string} id identity identifier
   * @return {promise}
   */
  getFormDefinition(id) {
    return RestApiService
      .get(this.getApiPath() + `/${id}/form-definition`)
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Returns filled form values
	 *
   * @param  {string} id identity identifier
   * @return {promise}
   */
  getFormValues(id) {
    return RestApiService
      .get(this.getApiPath() + `/${id}/form-values`)
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Saves form values
   *
   * @param  {string} id identity identifier
   * @param  {arrayOf(entity)} values filled form values
   * @return {promise}
   */
  saveFormValues(id, values) {
    return RestApiService
      .post(this.getApiPath() + `/${id}/form-values`, values)
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }
}

export default IdentityService;
