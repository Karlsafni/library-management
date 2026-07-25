

const SERVER_URL = (typeof window !== "undefined" && window.__BACKEND_URL__) || "https://library-backend-9r9n.onrender.com";

function addBook() {
    const title = document.getElementById("title").value;
    const author = document.getElementById("author").value;

    fetch(SERVER_URL + "/addBook", {
        method: "POST",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title: title, author: author})
    })
        .then(response => response.text())
        .then(text => {
            document.getElementById("message").innerHTML = text;
            document.getElementById("title").value = "";
            document.getElementById("author").value = "";
            loadDeleteBooks();
            searchBooks();
        })
        .catch(error => {
            document.getElementById("message").innerHTML = "Could not reach the server.";
            console.error(error);
        });

}

function getBooks() {
    return fetch(SERVER_URL + "/books")
        .then(response => response.text());
}

function stripDeleteButtons(html) {
    return html.replace(/<button[^>]*class=['"]delete-book-btn[^>]*>[\s\S]*?<\/button>/gi, "");
}

function renderBooks(containerId, includeDeleteButton) {
    getBooks()
        .then(html => {
            const container = document.getElementById(containerId);
            if (!container) {
                return;
            }

            if (includeDeleteButton) {
                container.innerHTML = html;
            } else {
                container.innerHTML = stripDeleteButtons(html);
            }
        })
        .catch(error => {
            const container = document.getElementById(containerId);
            if (container) {
                container.innerHTML = "Could not reach the server.";
            }
            console.error(error);
        });
}

function searchBooks() {
    const query = document.getElementById("searchInput").value.trim().toLowerCase();
    const container = document.getElementById("bookList");

    if (!container) {
        return;
    }

    getBooks()
        .then(html => {
            const temp = document.createElement("div");
            temp.innerHTML = stripDeleteButtons(html);
            const table = temp.querySelector(".bookTable");

            if (!table) {
                container.innerHTML = "<p>No books available.</p>";
                return;
            }

            const rows = Array.from(table.querySelectorAll("tbody .bookItem"));
            const filtered = rows.filter(item => {
                const text = item.textContent.toLowerCase();
                return text.includes(query);
            });

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
            container.innerHTML = "Could not reach the server.";
            console.error(error);
        });
}

function loadDeleteBooks() {
    renderBooks("deleteBookList", true);
}

function deleteBook(title, author) {
    fetch(SERVER_URL + "/deleteBook", {
        method: "POST",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title: title, author: author })
    })
        .then(response => response.text())
        .then(text => {
            document.getElementById("message").innerHTML = text;
            loadDeleteBooks();
            searchBooks();
        })
        .catch(error => {
            document.getElementById("message").innerHTML = "Could not delete the book.";
            console.error(error);
        });
}

function attachDeleteHandlers() {
    const bookList = document.getElementById("bookList");
    const deleteBookList = document.getElementById("deleteBookList");

    if (!bookList && !deleteBookList) {
        return;
    }

    const handleDeleteClick = function (event) {
        const button = event.target.closest(".delete-book-btn");
        if (!button) {
            return;
        }

        event.preventDefault();
        deleteBook(button.dataset.title, button.dataset.author);
    };

    if (bookList) {
        bookList.onclick = handleDeleteClick;
    }

    if (deleteBookList) {
        deleteBookList.onclick = handleDeleteClick;
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
    const sidebar = document.querySelector('.sidebar');
    const sidebarToggle = document.getElementById('sidebarToggle');

    if (sidebarToggle && sidebar) {
        sidebarToggle.addEventListener('click', () => {
            sidebar.classList.toggle('collapsed');
        });
    }

    document.querySelectorAll('.menu-item').forEach(button => {
        button.addEventListener('click', () => switchView(button.dataset.view));
    });

    attachDeleteHandlers();
    document.getElementById("bookList").innerHTML = "";
    document.getElementById("deleteBookList").innerHTML = "";
});