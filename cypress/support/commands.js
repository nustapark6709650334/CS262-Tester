Cypress.Commands.add('mockLogin', (program = 'special') => {
  const users = {
    special: {
      username: '6709650607',
      password: '1104300784873',
      responseUsername: '6709650607',
      redirect: 'main.html',
      token: 'fake-special-token',
    },
    normal: {
      username: '6709610607',
      password: '1104300784873',
      responseUsername: '6709610607',
      redirect: 'mainNormal.html',
      token: 'fake-normal-token',
    },
  };

  const user = users[program];

  cy.intercept('POST', '/api/auth/login', (req) => {
    expect(req.body).to.deep.equal({
      username: user.username,
      password: user.password,
    });

    req.reply({
      statusCode: 200,
      body: {
        message: 'Login successful',
        displayName: 'Cypress Student',
        email: `${user.username}@dome.tu.ac.th`,
        token: user.token,
        username: user.responseUsername,
      },
    });
  }).as('loginRequest');

  cy.visit('/index.html');
  cy.get('#username').should('be.visible').clear().type(user.username);
  cy.get('#password').clear().type(user.password);
  cy.get('#login-form').submit();
  cy.wait('@loginRequest').its('response.statusCode').should('eq', 200);
  cy.url().should('include', user.redirect);
  cy.window().its('localStorage.authToken').should('eq', user.token);

  return cy.wrap(user);
});

Cypress.Commands.add('seedAuth', (token = 'fake-special-token', mode = '/coursesS') => {
  cy.window().then((win) => {
    win.localStorage.setItem('authToken', token);
    win.sessionStorage.setItem('currentApiMode', mode);
  });
});

Cypress.Commands.add('visitAuthed', (path, options = {}) => {
  const token = options.token || 'fake-special-token';
  const mode = options.mode || '/coursesS';

  cy.visit(encodeURI(path), {
    ...options,
    onBeforeLoad(win) {
      win.localStorage.setItem('authToken', token);
      win.sessionStorage.setItem('currentApiMode', mode);

      if (options.onBeforeLoad) {
        options.onBeforeLoad(win);
      }
    },
  });
});

Cypress.Commands.add('assertLoggedOut', () => {
  cy.url().should('include', 'index.html');
  cy.window().then((win) => {
    expect(win.localStorage.getItem('authToken')).to.be.null;
    expect(win.sessionStorage.getItem('currentApiMode')).to.be.null;
  });
});
