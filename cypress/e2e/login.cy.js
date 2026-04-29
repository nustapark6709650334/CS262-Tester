describe('Login', () => {
  beforeEach(() => {
    cy.visit('/index.html');
    cy.window().then((win) => {
      win.localStorage.clear();
      win.sessionStorage.clear();
    });
  });

  it('logs in a special-program student and redirects to main.html', () => {
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: {
        message: 'Login successful',
        displayName: 'Cypress Special Student',
        email: '6709650607@dome.tu.ac.th',
        token: 'fake-special-token',
        username: '6709650607',
      },
    }).as('loginRequest');

    cy.get('#username').type('6709650607');
    cy.get('#password').type('1104300784873');
    cy.get('#login-form').submit();

    cy.wait('@loginRequest').then(({ request, response }) => {
      expect(request.body).to.deep.equal({
        username: '6709650607',
        password: '1104300784873',
      });
      expect(response.statusCode).to.eq(200);
    });

    cy.url().should('include', 'main.html');
    cy.window().its('localStorage.authToken').should('eq', 'fake-special-token');
  });

  it('logs in a normal-program student and redirects to mainNormal.html', () => {
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: {
        message: 'Login successful',
        displayName: 'Cypress Normal Student',
        email: '6709610607@dome.tu.ac.th',
        token: 'fake-normal-token',
        username: '6709610607',
      },
    }).as('loginRequest');

    cy.get('#username').type('6709610607');
    cy.get('#password').type('1104300784873');
    cy.get('#login-form').submit();

    cy.wait('@loginRequest').its('response.statusCode').should('eq', 200);
    cy.url().should('include', 'mainNormal.html');
    cy.window().its('localStorage.authToken').should('eq', 'fake-normal-token');
  });

  it('shows an error and does not create a token for bad credentials', () => {
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 401,
      body: {
        message: 'Username หรือ Password ไม่ถูกต้อง',
      },
    }).as('loginRequest');

    cy.get('#username').type('wrong');
    cy.get('#password').type('wrong');
    cy.get('#login-form').submit();

    cy.wait('@loginRequest').its('response.statusCode').should('eq', 401);
    cy.url().should('include', 'index.html');
    cy.get('#login-error')
      .should('be.visible')
      .and('contain', 'Username หรือ Password ไม่ถูกต้อง');
    cy.window().then((win) => {
      expect(win.localStorage.getItem('authToken')).to.be.null;
    });
  });

  it('shows a connection error when the login API is unavailable', () => {
    cy.intercept('POST', '/api/auth/login', { forceNetworkError: true }).as('loginRequest');

    cy.get('#username').type('6709650607');
    cy.get('#password').type('1104300784873');
    cy.get('#login-form').submit();

    cy.wait('@loginRequest');
    cy.url().should('include', 'index.html');
    cy.get('#login-error').should('be.visible').and('not.be.empty');
    cy.window().then((win) => {
      expect(win.localStorage.getItem('authToken')).to.be.null;
    });
  });
});
