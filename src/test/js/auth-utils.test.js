const path = require('path');
const { resolveProgramRoute } = require(
  path.resolve(__dirname, '../../main/resources/static/auth-utils.js')
);

// The routing rule mirrors login.js exactly:
//   username.substring(4, 6) === '65'  -> special -> main.html
//   username.substring(4, 6) === '61'  -> normal  -> mainNormal.html
//   otherwise                          -> unknown -> index.html
//
// Note: this is POSITIONAL. The code at the start of the username does NOT
// drive the route — only characters at index 4 and 5 do. Tests below use
// fabricated 8-char strings to make each position obvious.

describe('resolveProgramRoute (login.js routing spec)', () => {
  test('chars at index 4–5 = "65" -> special program -> main.html', () => {
    expect(resolveProgramRoute('ABCD65EF')).toEqual({
      program: 'special',
      url: 'main.html',
    });
  });

  test('chars at index 4–5 = "61" -> normal program -> mainNormal.html', () => {
    expect(resolveProgramRoute('ABCD61EF')).toEqual({
      program: 'normal',
      url: 'mainNormal.html',
    });
  });

  test('chars at index 4–5 != 65/61 -> unknown -> index.html', () => {
    expect(resolveProgramRoute('ABCD99EF')).toEqual({
      program: 'unknown',
      url: 'index.html',
    });
  });

  test('"65" at the START of username does NOT trigger special (rule is positional)', () => {
    // Starts with 65 but index 4–5 is "00" -> unknown
    expect(resolveProgramRoute('65090001')).toEqual({
      program: 'unknown',
      url: 'index.html',
    });
  });

  test('username shorter than 6 chars -> unknown -> index.html', () => {
    expect(resolveProgramRoute('12345')).toEqual({
      program: 'unknown',
      url: 'index.html',
    });
  });

  test('empty / null / undefined / non-string -> unknown -> index.html', () => {
    expect(resolveProgramRoute('')).toEqual({ program: 'unknown', url: 'index.html' });
    expect(resolveProgramRoute(null)).toEqual({ program: 'unknown', url: 'index.html' });
    expect(resolveProgramRoute(undefined)).toEqual({ program: 'unknown', url: 'index.html' });
    expect(resolveProgramRoute(65090001)).toEqual({ program: 'unknown', url: 'index.html' });
  });

  test('return shape is always { program, url } with the documented values', () => {
    const all = [
      resolveProgramRoute('ABCD65EF'),
      resolveProgramRoute('ABCD61EF'),
      resolveProgramRoute('nope'),
    ];
    for (const r of all) {
      expect(Object.keys(r).sort()).toEqual(['program', 'url']);
      expect(['special', 'normal', 'unknown']).toContain(r.program);
      expect(['main.html', 'mainNormal.html', 'index.html']).toContain(r.url);
    }
  });
});
