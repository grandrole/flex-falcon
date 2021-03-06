/**
 * LocalFunction
 *
 * @fileoverview
 *
 * @suppress {checkTypes}
 */

goog.provide('LocalFunction');



/**
 * @constructor
 */
LocalFunction = function() {
};


/**
 * @private
 * @type {string}
 */
LocalFunction.prototype.myMemberProperty = "got it: ";


/**
 * @private
 * @param {number} value
 */
LocalFunction.prototype.myMemberMethod = function(value) {
  function myLocalFunction(value) {
    return this.myMemberProperty + value;
  };
  org.apache.flex.utils.Language.trace("WOW! :: " + goog.bind(myLocalFunction, this)(value + 42));
};


/**
 * @expose
 */
LocalFunction.prototype.doIt = function() {
  this.myMemberMethod(624);
};


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
LocalFunction.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'LocalFunction', qName: 'LocalFunction'}] };

