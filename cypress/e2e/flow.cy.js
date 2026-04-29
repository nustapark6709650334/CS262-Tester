describe('Full User Journey', () => {
  it('logs in, searches, opens detail, visits curriculum, and logs out', () => {
    cy.mockLogin('special');

    cy.intercept('GET', '/api/coursesS*', { fixture: 'courses-special.json' }).as('searchCourses');
    cy.get('#search-input').type('100');
    cy.wait('@searchCourses').then(({ request }) => {
      expect(request.headers.authorization).to.eq('Bearer fake-special-token');
      expect(request.url).to.include('query=100');
    });
    cy.intercept('GET', '/api/coursesS/%E0%B8%84%E0%B8%9E.100', {
      fixture: 'course-cs100.json',
    }).as('courseDetail');
    cy.contains('#search-results .search-item', 'คพ.100').click();

    cy.wait('@courseDetail').its('response.body.courseName')
      .should('eq', 'คอมพิวเตอร์พื้นฐานและการโปรแกรมเบื้องต้น');
    cy.get('#course-header').should('contain', 'คพ.100');
    cy.get('#course-description').should('contain', 'พื้นฐานคอมพิวเตอร์');

    cy.visitAuthed('/main.html');
    cy.contains('.title-card', 'รายละเอียดหลักสูตร').within(() => {
      cy.contains('button', 'คลิก').click();
    });
    cy.url().should('include', 'curriculum.html');
    cy.contains('summary', 'ปีการศึกษา 1').click();
    cy.contains('summary', 'ปีการศึกษา 1')
      .parent('details')
      .within(() => {
        cy.contains('summary', 'เทอม 1').click();
        cy.contains('คพ.100').should('be.visible');
      });

    cy.get('#btnLogout').click();
    cy.assertLoggedOut();
  });
});
