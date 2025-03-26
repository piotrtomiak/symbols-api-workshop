Cypress.Commands.add('customAddedCommand', (url, directory, fileName) => {
  return cy.getCookies().then((cookies) => {
    return cy.task('downloadFile', {
      url,
      directory,
      cookies,
      fileName,
    })
  })
})

Cypress.Commands.overwrite('customAddedCommand', (url, directory) => {
  return cy.getCookies().then((cookies) => {
    return cy.task('downloadFile', {
      url,
      directory,
      cookies,
      fileName,
    })
  })
})

declare namespace Cypress {
  interface Chainable<Subject> {
    /**
     * Create several Todo items via UI
     * @example
     * cy.createDefaultTodos()
     */
    createDefaultTodos(): Chainable<any>

    /**
     * Creates one Todo using UI
     * @example
     * cy.createTodo('new item')
     */
    createTodo(title: string): Chainable<any>

    commandWithDefinition(title: String)
  }
}