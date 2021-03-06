import React from 'react';
import classNames from 'classnames';
import Joi from 'joi';
//
import AbstractFormComponent from '../AbstractFormComponent/AbstractFormComponent';
import Tooltip from '../Tooltip/Tooltip';
import HelpIcon from '../HelpIcon/HelpIcon';

class Checkbox extends AbstractFormComponent {

  constructor(props) {
    super(props);
  }

  onChange(event) {
    if (this.props.onChange) {
      this.props.onChange(event);
    }
    this.setState({
      value: event.currentTarget.checked
    }, () => {
      this.validate();
    });
  }

  /**
   * Focus input checkbox
   */
  focus() {
    this.refs.checkbox.focus();
  }

  getRequiredValidationSchema() {
    return Joi.boolean().valid(true);
  }

  getBody() {
    const { labelSpan, label, componentSpan, tooltip, help, helpBlock } = this.props;
    const { value, readOnly, disabled } = this.state;
    const title = this.getValidationResult() && this.getValidationResult().message ? this.getValidationResult().message + ' (' + tooltip + ')' : tooltip;
    const className = classNames(
      labelSpan,
      componentSpan
    );

    return (
      <div className={className}>
        <div className="checkbox">
          {/* focus can not be added for checkbox - event colision when checkbox  */}
          <Tooltip trigger={['click', 'hover']} ref="popover" placement="left" value={title}>
            <label>
              <input
                type="checkbox"
                ref="checkbox"
                disabled={readOnly || disabled}
                onChange={this.onChange}
                checked={value}
                readOnly={readOnly}/>
              <span>
                {label}
              </span>
            </label>
          </Tooltip>
          {this.props.children}
          <HelpIcon content={help} style={{ marginLeft: '3px' }}/>
          {
            !helpBlock
            ||
            <span className="help-block" style={{ whiteSpace: 'normal' }}>{helpBlock}</span>
          }
        </div>
      </div>
    );
  }
}

Checkbox.propTypes = {
  ...AbstractFormComponent.propTypes
};

Checkbox.defaultProps = {
  ...AbstractFormComponent.defaultProps,
  labelSpan: 'col-sm-offset-3'
};


export default Checkbox;
