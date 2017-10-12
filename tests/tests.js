/* global Fingerprint */
/* eslint-disable no-alert, no-console */

exports.defineAutoTests = function() {
  describe("Fingerprint Object", function () {
    it("should exist", function() {
      expect(window.Fingerprint).toBeDefined();
    });
  });

  describe("isAvailable", function () {
    it("isAvailable schould be defined", function () {
      expect(window.Fingerprint.isAvailable).toBeDefined();
    });

    it("isAvailable schould return an result or error in callback", function (done) {
      window.Fingerprint.isAvailable( function (result) {
        expect(result).toBeDefined();
        done();
      }, function(result) {
        expect(result).toBeDefined();
        done();
      });
    });
  });

  describe("show", function () {
    it("show schould be defined", function () {
      expect(window.Fingerprint.show).toBeDefined();
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
