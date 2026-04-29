describe('Course Detail', () => {
  it('loads course name, description, credit, prerequisite, and next courses', () => {
    cy.intercept('GET', '/api/coursesS/%E0%B8%84%E0%B8%9E.111', {
      fixture: 'course-cs111.json',
    }).as('courseDetail');

    cy.visitAuthed('/courses-detail.html?course=คพ.111');

    cy.wait('@courseDetail').then(({ request, response }) => {
      expect(request.headers.authorization).to.eq('Bearer fake-special-token');
      expect(response.statusCode).to.eq(200);
      expect(response.body.courseCode).to.eq('คพ.111');
    });

    cy.url().should('include', 'courses-detail.html');
    cy.get('#course-header')
      .should('contain', 'คพ.111')
      .and('contain', 'แนวคิดเชิงวัตถุ');
    cy.get('#course-credit').should('contain', '3');
    cy.get('#course-description').should('contain', 'พัฒนาซอฟต์แวร์');
    cy.get('#prerequisite-courses').should('contain', 'คพ.102');
    cy.get('#next-courses').should('contain', 'คพ.216').and('contain', 'คพ.261');
  });

  it('shows no prerequisite when course permission is empty', () => {
    cy.intercept('GET', '/api/coursesS/%E0%B8%84%E0%B8%9E.100', {
      fixture: 'course-cs100.json',
    }).as('courseDetail');

    cy.visitAuthed('/courses-detail.html?course=คพ.100');

    cy.wait('@courseDetail').its('response.statusCode').should('eq', 200);
    cy.get('#course-header').should('contain', 'คพ.100');
    cy.get('#prerequisite-courses').should('contain', 'ไม่มี');
    cy.get('#next-courses').should('contain', 'คพ.102');
  });

  it('shows a missing-course message when no course query is provided', () => {
    cy.visitAuthed('/courses-detail.html');

    cy.get('#course-header').should('contain', 'ไม่พบรายวิชา');
    cy.get('#course-description').should('contain', 'กำลังโหลด');
  });

  it('renders an error message when the detail API fails', () => {
    cy.intercept('GET', '/api/coursesS/UNKNOWN', {
      statusCode: 404,
      body: { message: 'Not found' },
    }).as('courseDetailFail');

    cy.visitAuthed('/courses-detail.html?course=UNKNOWN');

    cy.wait('@courseDetailFail').its('response.statusCode').should('eq', 404);
    cy.get('#course-header').should('not.contain', 'กำลังโหลด');
    cy.get('#course-description').should('contain', 'UNKNOWN');
  });

  it('redirects to login and clears token when detail API returns 401', () => {
    cy.intercept('GET', '/api/coursesS/%E0%B8%84%E0%B8%9E.100', {
      statusCode: 401,
      body: { message: 'Unauthorized' },
    }).as('unauthorizedDetail');

    cy.visitAuthed('/courses-detail.html?course=คพ.100');
    cy.wait('@unauthorizedDetail').its('response.statusCode').should('eq', 401);

    cy.url().should('include', 'index.html');
    cy.window().then((win) => {
      expect(win.localStorage.getItem('authToken')).to.be.null;
    });
  });
});
