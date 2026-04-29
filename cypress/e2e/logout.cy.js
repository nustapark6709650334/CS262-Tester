describe('Logout', () => {
  it('logs out from main page and clears browser storage', () => {
    cy.visitAuthed('/main.html');

    cy.get('#btnLogout').should('be.visible').click();

    cy.assertLoggedOut();
  });

  it('logs out from course detail page after data has loaded', () => {
    cy.intercept('GET', '/api/coursesS/%E0%B8%84%E0%B8%9E.100', {
      fixture: 'course-cs100.json',
    }).as('courseDetail');

    cy.visitAuthed('/courses-detail.html?course=คพ.100');
    cy.wait('@courseDetail');

    cy.get('#btnLogout').click();

    cy.assertLoggedOut();
  });

  it('blocks protected pages when auth token is missing', () => {
    cy.on('uncaught:exception', (error) => {
      expect(error.message).to.include('Authentication required');
      return false;
    });

    cy.visit('/main.html');

    cy.url().should('include', 'index.html');
    cy.window().then((win) => {
      expect(win.localStorage.getItem('authToken')).to.be.null;
    });
  });
});
