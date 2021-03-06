goog.provide('Example');

goog.require('flash.events.MouseEvent');
goog.require('spark.components.Button');
goog.require('spark.components.Group');
goog.require('spark.components.Label');

/**
 * @constructor
 * @extends {spark.components.Group}
 */
Example = function() {
	var self = this;
	goog.base(this);
	self.init();
}
goog.inherits(Example, spark.components.Group);

/**
 * @private
 * @const
 * @type {string}
 */
Example.BYEBYE = "Bye Bye";

/**
 * @private
 * @const
 * @type {string}
 */
Example.HELLOWORLD = "Hello World";

/**
 * @private
 * @type {number}
 */
Example.counter = 100;

/**
 * @private
 * @type {spark.components.Button}
 */
Example.prototype._btn1;

/**
 * @private
 * @type {spark.components.Button}
 */
Example.prototype._btn2;

/**
 * @private
 * @type {spark.components.Button}
 */
Example.prototype._btn3;

/**
 * @private
 * @type {spark.components.Label}
 */
Example.prototype._lbl1;

/**
 * @private
 * @type {spark.components.Label}
 */
Example.prototype._lbl2;

Example.prototype.init = function() {
	var self = this;
	self._lbl1 = new spark.components.Label();
	self._lbl1.x = 100;
	self._lbl1.y = 25;
	self._lbl1.text = Example.HELLOWORLD;
	self.addElement(self._lbl1);
	self._lbl2 = new spark.components.Label();
	self._lbl2.x = 200;
	self._lbl2.y = 25;
	self._lbl2.text = Example.counter + "";
	self.addElement(self._lbl2);
	self._btn1 = new spark.components.Button();
	self._btn1.x = 100;
	self._btn1.y = 50;
	self._btn1.label = "Click me";
	self._btn1.addEventListener(flash.events.MouseEvent.CLICK, self.btn1clickHandler);
	self.addElement(self._btn1);
	self._btn2 = new spark.components.Button();
	self._btn2.x = 200;
	self._btn2.y = 50;
	self._btn2.label = "Add it";
	self._btn2.addEventListener(flash.events.MouseEvent.CLICK, self.btn2clickHandler);
	self.addElement(self._btn2);
	self._btn3 = new spark.components.Button();
	self._btn3.x = 300;
	self._btn3.y = 50;
	self._btn3.label = "Move it";
	self._btn3.addEventListener(flash.events.MouseEvent.CLICK, self.btn3clickHandler);
	self.addElement(self._btn3);
};

/**
 * @param {flash.events.MouseEvent} event
 */
Example.prototype.btn1clickHandler = function(event) {
	var self = this;
	if (self._lbl1.text == Example.HELLOWORLD)
		self._lbl1.text = Example.BYEBYE;
	else
		self._lbl1.text = Example.HELLOWORLD;
};

/**
 * @param {flash.events.MouseEvent} event
 */
Example.prototype.btn2clickHandler = function(event) {
	var self = this;
	self._lbl2.text = --Example.counter + "";
};

/**
 * @param {flash.events.MouseEvent} event
 */
Example.prototype.btn3clickHandler = function(event) {
	var self = this;
	self._btn3.x += 10;
};