describe('Course Search', () => {
  beforeEach(() => {
    cy.intercept('GET', '/api/coursesS*', { fixture: 'courses-special.json' }).as('searchCourses');
    cy.visitAuthed('/main.html');
  });

  it('searches by course number and displays matching courses', () => {
    cy.get('#search-input').should('be.visible').type('100');

    cy.wait('@searchCourses').then(({ request, response }) => {
      expect(request.url).to.include('/api/coursesS?query=100');
      expect(request.headers.authorization).to.eq('Bearer fake-special-token');
      expect(response.statusCode).to.eq(200);
      expect(response.body[0].courseCode).to.eq('คพ.100');
    });

    cy.get('#search-results')
      .should('be.visible')
      .and('contain', 'คพ.100')
      .and('contain', 'คอมพิวเตอร์พื้นฐานและการโปรแกรมเบื้องต้น');
  });

  it('normalizes Thai course-code input before calling the API', () => {
    cy.get('#search-input').type('คพ. 100');

    cy.wait('@searchCourses').then(({ request }) => {
      expect(decodeURIComponent(request.url)).to.include('query=CS100');
    });

    cy.get('#search-results').should('contain', 'คพ.100');
  });

  it('opens course detail from a search result', () => {
    cy.get('#search-input').type('100');
    cy.wait('@searchCourses');

    cy.contains('#search-results .search-item', 'คพ.100').click();

    cy.url().should('include', 'courses-detail.html');
    cy.url().should('include', 'course=%E0%B8%84%E0%B8%9E.100');
    cy.window().its('localStorage.authToken').should('eq', 'fake-special-token');
  });

  it('shows a no-results message for unmatched search text', () => {
    cy.intercept('GET', '/api/coursesS*', []).as('emptySearch');

    cy.get('#search-input').type('zz-not-found');

    cy.wait('@emptySearch').its('response.statusCode').should('eq', 200);
    cy.get('#search-results').should('be.visible').and('contain', 'ไม่พบรายวิชา');
  });

  it('does not call search API for a one-character query', () => {
    cy.clock();
    cy.get('#search-input').type('1');
    cy.tick(401);

    cy.get('#search-results').should('not.be.visible');
    cy.get('@searchCourses.all').should('have.length', 0);
  });

  it('redirects to login and clears token when search API returns 401', () => {
    cy.intercept('GET', '/api/coursesS*', {
      statusCode: 401,
      body: { message: 'Unauthorized' },
    }).as('unauthorizedSearch');

    cy.get('#search-input').type('100');
    cy.wait('@unauthorizedSearch').its('response.statusCode').should('eq', 401);

    cy.url().should('include', 'index.html');
    cy.window().then((win) => {
      expect(win.localStorage.getItem('authToken')).to.be.null;
    });
  });
});
