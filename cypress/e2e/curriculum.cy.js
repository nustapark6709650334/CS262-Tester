describe('Curriculum', () => {
  it('navigates from main page to the special-program curriculum', () => {
    cy.visitAuthed('/main.html');

    cy.contains('.title-card', 'รายละเอียดหลักสูตร').within(() => {
      cy.contains('button', 'คลิก').click();
    });

    cy.url().should('include', 'curriculum.html');
    cy.contains('h2', 'หลักสูตรปี 66').should('be.visible');
    cy.window().its('sessionStorage.currentApiMode').should('eq', '/coursesS');
  });

  it('opens year 1 term 1 and validates course data', () => {
    cy.visitAuthed('/curriculum.html');

    cy.contains('summary', 'ปีการศึกษา 1').click();
    cy.contains('summary', 'ปีการศึกษา 1')
      .parent('details')
      .should('have.attr', 'open');

    cy.contains('summary', 'ปีการศึกษา 1')
      .parent('details')
      .within(() => {
        cy.contains('summary', 'เทอม 1').click();
        cy.contains('summary', 'เทอม 1')
          .parent('details')
          .should('have.attr', 'open');
        cy.contains('td', 'คพ.100')
          .parent('tr')
          .within(() => {
            cy.contains('คอมพิวเตอร์พื้นฐาน').should('exist');
            cy.contains('.badge-sks', '3').should('exist');
          });
        cy.contains('หน่วยกิตรวม').parent('tr').should('contain', '21');
      });
  });

  it('opens a curriculum course into the detail page', () => {
    cy.visitAuthed('/curriculum.html');

    cy.contains('summary', 'ปีการศึกษา 1').click();
    cy.contains('summary', 'ปีการศึกษา 1')
      .parent('details')
      .within(() => {
        cy.contains('summary', 'เทอม 1').click();
        cy.contains('.subject', 'คพ.100').click();
      });

    cy.url().should('include', 'courses-detail.html');
    cy.url().should('include', 'course=CS100');
  });

  it('redirects unauthenticated curriculum access to login', () => {
    cy.on('uncaught:exception', (error) => {
      expect(error.message).to.include('Authentication required');
      return false;
    });

    cy.visit('/curriculum.html');

    cy.url().should('include', 'index.html');
    cy.window().then((win) => {
      expect(win.localStorage.getItem('authToken')).to.be.null;
    });
  });
});
