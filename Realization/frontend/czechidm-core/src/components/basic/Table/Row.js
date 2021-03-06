import React, { PropTypes } from 'react';
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Cell from './Cell';

/**
 * Component that renders the row for <Table />.
 * This component should not be used directly by developer. Instead,
 * only <Table /> should use the component internally.
 */
class Row extends AbstractComponent {

  constructor(props) {
    super(props);
  }

  _onClick(/* object */ event) {
    this.props.onClick(event, this.props.rowIndex, this.props.data);
  }

  _onDoubleClick(/* object */ event) {
    this.props.onDoubleClick(event, this.props.rowIndex, this.props.data);
  }

  _onSelect(event) {
    const { onRowSelect, rowIndex } = this.props;
    if (!onRowSelect) {
      return;
    }
    onRowSelect(rowIndex, event.currentTarget.checked);
  }

  _getTitle({ selected, rowIndex }) {
    // TODO: localization - move to props adn fill in advanced table
    if (selected) {
      return 'Zrušit výběr';
    }
    if (rowIndex !== undefined && rowIndex !== null && rowIndex > -1) {
      return 'Vybrat záznam';
    }
    return 'Vybrat záznamy';
  }

  render() {
    const { rowIndex, columns, selected, rowClass } = this.props;
    const cells = new Array(columns.length);
    for (let i = 0, j = columns.length; i < j; i++) {
      const columnProps = columns[i].props;
      cells[i] = (
        <Cell
          key={`cell_${i}`}
          rowIndex={rowIndex}
          cell={columnProps.cell}
          property={columnProps.property}
          data={this.props.data}
          showLoading={this.props.showLoading}
          width={columnProps.width}
          className={columnProps.className}
        />
      );
    }
    let _rowClass;
    if (!rowClass) {
      _rowClass = '';
    } else if (typeof rowClass === 'function') {
      _rowClass = rowClass({
        rowIndex,
        data: this.props.data
      });
    } else {
      _rowClass = rowClass;
    }

    return (
      <tr
        onClick={this.props.onClick ? this._onClick.bind(this) : null}
        onDoubleClick={this.props.onDoubleClick ? this._onDoubleClick.bind(this) : null}
        className={_rowClass}>
        {
          !this.props.onRowSelect
          ||
          <td width="16px" className="bulk-selection">
            <input
              type="checkbox"
              checked={selected}
              onChange={this._onSelect.bind(this)}
              title={this._getTitle(this.props)}/>
          </td>
        }
        {cells}
      </tr>
    );
  }
}

Row.propTypes = {
  rowIndex: PropTypes.number.isRequired,
  columns: PropTypes.array.isRequired,
  /**
   * loadinig indicator
   */
  showLoading: React.PropTypes.bool,
  /**
   * input data as array of json objects
   */
  data: PropTypes.array,
  /**
   * Callback that is called when a row is clicked.
   */
  onClick: PropTypes.func,

  /**
   * Callback that is called when a row is double clicked.
   */
  onDoubleClick: PropTypes.func,
  /**
   * Row selection
   */
  onRowSelect: React.PropTypes.func,
  /**
   * If row is selected
   */
  selected: React.PropTypes.bool,
  /**
   * css added to row
   */
  rowClass: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.func
  ])
};
Row.defaultProps = {
  showLoading: false,
  selected: false
};

export default Row;
