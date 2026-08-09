const SERVER_URL = (typeof window !== "undefined" && window.__BACKEND_URL__) || "https://library-backend-9r9n.onrender.com";

let messageTimeout;
function showMessage(text) {
    const msgEl = document.getElementById("message");
    if (!msgEl) return;
    
    msgEl.innerHTML = text;
    msgEl.classList.add("show");
    
    if (messageTimeout) {
        clearTimeout(messageTimeout);
    }
    
    messageTimeout = setTimeout(() => {
        msgEl.classList.remove("show");
    }, 4000);
}

function addBook() {
    const title = document.getElementById("title").value.trim();
    const author = document.getElementById("author").value.trim();
    const copiesInput = document.getElementById("copies");
    const copies = copiesInput ? copiesInput.value.trim() : "1";

    if (!title || !author) {
        showMessage("Please enter both title and author.");
        return;
    }

    fetch(SERVER_URL + "/addBook", {
        method: "POST",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title: title, author: author, copies: copies })
    })
        .then(response => response.text())
        .then(text => {
            showMessage(text);
            document.getElementById("title").value = "";
            document.getElementById("author").value = "";
            if (copiesInput) copiesInput.value = "1";
            loadBookList();
        })
        .catch(error => {
            showMessage("Could not reach the server.");
            console.error(error);
        });
}

function getBooks() {
    return fetch(SERVER_URL + "/books")
        .then(response => response.text());
}

// Consolidates retrieval, filtering, rendering, and action states for books
function loadBookList() {
    const searchInput = document.getElementById("searchInput");
    const searchVal = searchInput ? searchInput.value.trim().toLowerCase() : "";
    const container = document.getElementById("bookList");
    
    if (!container) return;

    getBooks()
        .then(html => {
            const temp = document.createElement("div");
            temp.innerHTML = html;
            const table = temp.querySelector(".bookTable");

            if (!table) {
                container.innerHTML = "<p>No books available.</p>";
                return;
            }

            const rows = Array.from(table.querySelectorAll("tbody .bookItem"));
            
            const filtered = searchVal 
                ? rows.filter(item => item.textContent.toLowerCase().includes(searchVal))
                : rows;

            if (filtered.length === 0) {
                container.innerHTML = "<p>No matching books found.</p>";
                return;
            }

            const updatedTable = table.cloneNode(true);
            const updatedBody = updatedTable.querySelector("tbody");
            updatedBody.innerHTML = filtered.map(item => item.outerHTML).join("");
            container.innerHTML = updatedTable.outerHTML;
        })
        .catch(error => {
            container.innerHTML = "<p>Could not reach the server.</p>";
            console.error(error);
        });
}

function searchBooks() {
    loadBookList();
}

function deleteBook(title, author) {
    fetch(SERVER_URL + "/deleteBook", {
        method: "POST",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title: title, author: author })
    })
        .then(response => response.text())
        .then(text => {
            showMessage(text);
            loadBookList();
            loadBorrowerList();
        })
        .catch(error => {
            showMessage("Could not delete the book.");
            console.error(error);
        });
}

function borrowBook(title, author) {
    const borrower = prompt("Enter borrower's name:");
    if (borrower === null) return; // cancelled prompt
    
    const borrowerTrimmed = borrower.trim();
    if (!borrowerTrimmed) {
        showMessage("Borrower name cannot be empty.");
        return;
    }

    fetch(SERVER_URL + "/borrowBook", {
        method: "POST",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title: title, author: author, borrower: borrowerTrimmed })
    })
        .then(response => response.text())
        .then(text => {
            showMessage(text);
            loadBookList();
            loadBorrowerList();
        })
        .catch(error => {
            showMessage("Could not borrow the book.");
            console.error(error);
        });
}

function getBorrowers() {
    return fetch(SERVER_URL + "/borrowers")
        .then(response => response.text());
}

function loadBorrowerList() {
    const searchInput = document.getElementById("borrowerSearchInput");
    const searchVal = searchInput ? searchInput.value.trim().toLowerCase() : "";
    const container = document.getElementById("borrowerList");
    
    if (!container) return;

    getBorrowers()
        .then(html => {
            const temp = document.createElement("div");
            temp.innerHTML = html;
            const table = temp.querySelector(".bookTable");

            if (!table) {
                container.innerHTML = "<p>No active borrowers.</p>";
                return;
            }

            const rows = Array.from(table.querySelectorAll("tbody .bookItem"));
            
            const filtered = searchVal 
                ? rows.filter(item => item.textContent.toLowerCase().includes(searchVal))
                : rows;

            if (filtered.length === 0) {
                container.innerHTML = "<p>No matching borrowers found.</p>";
                return;
            }

            const updatedTable = table.cloneNode(true);
            const updatedBody = updatedTable.querySelector("tbody");
            updatedBody.innerHTML = filtered.map(item => item.outerHTML).join("");
            container.innerHTML = updatedTable.outerHTML;
        })
        .catch(error => {
            container.innerHTML = "<p>Could not reach the server.</p>";
            console.error(error);
        });
}

function searchBorrowers() {
    loadBorrowerList();
}

function returnBook(borrower, title, author) {
    fetch(SERVER_URL + "/returnBook", {
        method: "POST",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title: title, author: author, borrower: borrower })
    })
        .then(response => response.text())
        .then(text => {
            showMessage(text);
            loadBookList();
            loadBorrowerList();
        })
        .catch(error => {
            showMessage("Could not return the book.");
            console.error(error);
        });
}

function attachEventHandlers() {
    const bookList = document.getElementById("bookList");
    if (bookList) {
        bookList.onclick = function (event) {
            const deleteBtn = event.target.closest(".delete-book-btn");
            if (deleteBtn) {
                event.preventDefault();
                deleteBook(deleteBtn.dataset.title, deleteBtn.dataset.author);
                return;
            }

            const borrowBtn = event.target.closest(".borrow-book-btn");
            if (borrowBtn) {
                event.preventDefault();
                borrowBook(borrowBtn.dataset.title, borrowBtn.dataset.author);
                return;
            }
        };
    }

    const borrowerList = document.getElementById("borrowerList");
    if (borrowerList) {
        borrowerList.onclick = function (event) {
            const returnBtn = event.target.closest(".return-book-btn");
            if (returnBtn) {
                event.preventDefault();
                returnBook(returnBtn.dataset.borrower, returnBtn.dataset.title, returnBtn.dataset.author);
                return;
            }
        };
    }
}

function switchView(viewName) {
    document.querySelectorAll('.view').forEach(view => view.classList.remove('active'));
    document.querySelectorAll('.menu-item').forEach(button => button.classList.remove('active'));

    const targetView = document.getElementById(viewName + '-view');
    const targetButton = document.querySelector(`[data-view="${viewName}"]`);

    if (targetView) {
        targetView.classList.add('active');
    }

    if (targetButton) {
        targetButton.classList.add('active');
    }
}

window.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll('.menu-item').forEach(button => {
        button.addEventListener('click', () => {
            const viewName = button.dataset.view;
            switchView(viewName);
            if (viewName === 'books') {
                const searchInput = document.getElementById("searchInput");
                if (searchInput) searchInput.value = "";
                loadBookList();
            } else if (viewName === 'borrowers') {
                const bSearchInput = document.getElementById("borrowerSearchInput");
                if (bSearchInput) bSearchInput.value = "";
                loadBorrowerList();
            }
        });
    });

    attachEventHandlers();
    loadBookList();
    loadBorrowerList();
});