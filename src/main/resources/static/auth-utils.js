// Pure auth helpers for the CSTU Courses Explorer frontend.
// UMD wrapper so the same file is consumable as a <script> tag in the browser
// (exposes window.AuthUtils) and as a CommonJS module in Jest.
(function (root, factory) {
  if (typeof module === 'object' && module.exports) {
    module.exports = factory();
  } else {
    root.AuthUtils = factory();
  }
}(typeof self !== 'undefined' ? self : this, function () {

  // Spec (from the login requirements):
  //   username[4..6] === '65'  -> special program (ภาคพิเศษ) -> main.html
  //   username[4..6] === '61'  -> normal  program (ภาคปกติ)  -> mainNormal.html
  //   anything else            -> unknown -> index.html
  function resolveProgramRoute(username) {
    if (typeof username !== 'string' || username.length < 6) {
      return { program: 'unknown', url: 'index.html' };
    }
    var code = username.substring(4, 6);
    if (code === '65') return { program: 'special', url: 'main.html' };
    if (code === '61') return { program: 'normal',  url: 'mainNormal.html' };
    return { program: 'unknown', url: 'index.html' };
  }

  return {
    resolveProgramRoute: resolveProgramRoute
  };
}));
