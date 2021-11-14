/**
 * 
 */

print("hello world from script for BOA....");
print("welcome....");

var printName = function(name) {
	print('Hi there from Javascript, ' + name);
	return "greetings from javascript";
};

var printJavaObject = function(object) {
	print("JS Class Definition: " + Object.prototype.toString.call(object));
};

var callingJavaFunction = function() {
	var NashornExamples = Java
			.type('com.mslc.training.java8.newfeatures.NashornExamples');
	
	print(NashornExamples);
	var result = NashornExamples.javaFunction('javascript-object');
	print("\n****" + result + "***");

}