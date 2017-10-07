exports.defineAutoTests = function() {
  describe('Fingerprint Object', function () {
    it("should exist", function() {
      expect(window.Fingperprint).toBeDefined();
    });
  });
};
