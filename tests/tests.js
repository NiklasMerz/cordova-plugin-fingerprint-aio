/* global Fingerprint cordova device */
/* eslint-disable no-alert, no-console */

exports.defineAutoTests = function () {
  var handlers;

  describe("Fingerprint Object", function () {
    it("should exist", function () {
      expect(window.Fingerprint).toBeDefined();
    });
  });

  if (cordova.platformId === "android" && parseFloat(device.version) <= 5.1) {
    describe("Android platform not supported", function () {
      beforeEach(function () {
        handlers = {
          successHandler: function () { },
          errorHandler: function () { }
        };
      });

      it("should call the error handler when attempting to use the plugin on Android 5.1 or below", function (done) {
        spyOn(handlers, "errorHandler").and.callFake(function (res) {
          expect(res).toEqual("minimum SDK version 23 required");
          expect(handlers.successHandler).not.toHaveBeenCalled();
          done();
        });
        spyOn(handlers, "successHandler");

        Fingerprint.show({
          clientId: "Fingerprint-Tests",
          clientSecret: "password",
          disableBackup: true
        }, handlers.successHandler, handlers.errorHandler);
      });
    });
    return; // skip all other tests
  }

  beforeEach(function () {
    handlers = {
      successHandler: function () { },
      errorHandler: function () { }
    };
  });

  describe("isAvailable", function () {
    it("schould be defined", function () {
      expect(window.Fingerprint.isAvailable).toBeDefined();
    });

    it("schould return an result or error in callback", function (done) {
      spyOn(handlers, "successHandler").and.callFake(function (res) {
        console.log(res);
        expect(res).toBeDefined();
        done();
      });

      spyOn(handlers, "errorHandler").and.callFake(function (res) {
        console.log(res);
        expect(res).toBeDefined();
        done();
      });

      Fingerprint.isAvailable(handlers.successHandler, handlers.errorHandler);
    });
  });


  describe("show", function () {
    it("show schould be defined", function () {
      expect(window.Fingerprint.show).toBeDefined();
    });

    it("should be able call with error or success", function () {
      //UI interaction needed for dialog
      Fingerprint.show({
        clientId: "Fingerprint-Tests",
        clientSecret: "password",
        disableBackup: false
      }, handlers.successHandler, handlers.errorHandler);
    });
  });
};

exports.defineManualTests = function (contentEl, createActionButton) {

  createActionButton("isAvailable", function () {
    window.Fingerprint.isAvailable(isAvailableSuccess, isAvailableError);

    function isAvailableSuccess(result) {
      console.log(result);
      alert("Fingerprint available");
    }

    function isAvailableError(message) {
      alert(message);
    }
  });

  createActionButton("show", function () {
    Fingerprint.show({
      clientId: "Fingerprint-Tests",
      clientSecret: "password",
      disableBackup: false
    }, successCallback, errorCallback);

    function successCallback() {
      alert("Authentication successfull");
    }

    function errorCallback(err) {
      alert("Authentication invalid " + err);
    }
  });

  createActionButton("show-disablebackup", function () {
    Fingerprint.show({
      clientId: "Fingerprint-Tests",
      clientSecret: "password",
      disableBackup: true
    }, successCallback, errorCallback);

    function successCallback() {
      alert("Authentication successfull");
    }

    function errorCallback(err) {
      alert("Authentication invalid " + err);
    }
  });

};
