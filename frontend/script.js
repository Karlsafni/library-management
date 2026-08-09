const SERVER_URL = (typeof window !== "undefined" && window.__BACKEND_URL__) || "https://library-management-backend-zqf7.onrender.com";

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
                container.innerHTML = `
                    <table class="bookTable">
                        <thead>
                            <tr><th>S.No</th><th>Book Name</th><th>Author Name</th><th>No of Copies</th><th>Borrowed By</th><th>Action</th></tr>
                        </thead>
                        <tbody>
                            <tr><td colspan="6" class="empty-table-cell">No books available.</td></tr>
                        </tbody>
                    </table>`;
                return;
            }

            const rows = Array.from(table.querySelectorAll("tbody .bookItem"));
            
            const filtered = searchVal 
                ? rows.filter(item => item.textContent.toLowerCase().includes(searchVal))
                : rows;

            const updatedTable = table.cloneNode(true);
            const updatedBody = updatedTable.querySelector("tbody");

            if (filtered.length === 0) {
                const message = searchVal ? "No matching books found." : "No books available.";
                updatedBody.innerHTML = `<tr><td colspan="6" class="empty-table-cell">${message}</td></tr>`;
            } else {
                updatedBody.innerHTML = filtered.map(item => item.outerHTML).join("");
            }
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

let activeBorrowTitle = "";
let activeBorrowAuthor = "";

function borrowBook(title, author) {
    activeBorrowTitle = title;
    activeBorrowAuthor = author;
    
    const modal = document.getElementById("borrowModal");
    const modalInfo = document.getElementById("borrowModalBookInfo");
    if (modal) {
        if (modalInfo) {
            modalInfo.textContent = `Book: "${title}" by ${author}`;
        }
        
        // Set default start date to today
        const startDateInput = document.getElementById("borrowStartDate");
        if (startDateInput && !startDateInput.value) {
            const today = new Date().toISOString().split('T')[0];
            startDateInput.value = today;
        }
        
        modal.classList.add("show");
    }
}

function closeBorrowModal() {
    const modal = document.getElementById("borrowModal");
    if (modal) {
        modal.classList.remove("show");
        
        // Clear fields
        document.getElementById("borrowerNameInput").value = "";
        document.getElementById("borrowStartDate").value = "";
        document.getElementById("borrowEndDate").value = "";
        
        activeBorrowTitle = "";
        activeBorrowAuthor = "";
    }
}

function submitBorrowForm() {
    const borrower = document.getElementById("borrowerNameInput").value.trim();
    const startDate = document.getElementById("borrowStartDate").value;
    const endDate = document.getElementById("borrowEndDate").value;

    if (!borrower) {
        showMessage("Please enter the borrower's name.");
        return;
    }
    if (!startDate || !endDate) {
        showMessage("Please select both start and end dates.");
        return;
    }

    fetch(SERVER_URL + "/borrowBook", {
        method: "POST",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            title: activeBorrowTitle,
            author: activeBorrowAuthor,
            borrower: borrower,
            startDate: startDate,
            endDate: endDate
        })
    })
        .then(response => response.text())
        .then(text => {
            showMessage(text);
            closeBorrowModal();
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
                container.innerHTML = `
                    <table class="bookTable">
                        <thead>
                            <tr><th>S.No</th><th>Borrower's Name</th><th>Book Name</th><th>Author Name</th><th>Start Date</th><th>End Date</th><th>Action</th></tr>
                        </thead>
                        <tbody>
                            <tr><td colspan="7" class="empty-table-cell">No active borrowers.</td></tr>
                        </tbody>
                    </table>`;
                return;
            }

            const rows = Array.from(table.querySelectorAll("tbody .bookItem"));
            
            const filtered = searchVal 
                ? rows.filter(item => item.textContent.toLowerCase().includes(searchVal))
                : rows;

            const updatedTable = table.cloneNode(true);
            const updatedBody = updatedTable.querySelector("tbody");

            if (filtered.length === 0) {
                const message = searchVal ? "No matching borrowers found." : "No active borrowers.";
                updatedBody.innerHTML = `<tr><td colspan="7" class="empty-table-cell">${message}</td></tr>`;
            } else {
                updatedBody.innerHTML = filtered.map(item => item.outerHTML).join("");
            }
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

    const memberList = document.getElementById("memberList");
    if (memberList) {
        memberList.onclick = function (event) {
            const deleteBtn = event.target.closest(".delete-member-btn");
            if (deleteBtn) {
                event.preventDefault();
                deleteMember(deleteBtn.dataset.name, deleteBtn.dataset.phone);
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

function addMember() {
    const nameEl = document.getElementById("memberName");
    const phoneEl = document.getElementById("memberPhone");
    const altPhoneEl = document.getElementById("memberAltPhone");
    const addressEl = document.getElementById("memberAddress");

    if (!nameEl || !phoneEl || !altPhoneEl || !addressEl) return;

    const name = nameEl.value.trim();
    const phone = phoneEl.value.trim();
    const altPhone = altPhoneEl.value.trim();
    const address = addressEl.value.trim();

    if (!name || !phone) {
        showMessage("Name and Phone Number are required.");
        return;
    }

    fetch(SERVER_URL + "/addMember", {
        method: "POST",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, phone, altPhone, address })
    })
        .then(response => response.text())
        .then(text => {
            showMessage(text);
            if (text.includes("Successfully")) {
                nameEl.value = "";
                phoneEl.value = "";
                altPhoneEl.value = "";
                addressEl.value = "";
                loadMemberList();
            }
        })
        .catch(error => {
            showMessage("Could not add member.");
            console.error(error);
        });
}

function getMembers() {
    return fetch(SERVER_URL + "/members")
        .then(response => response.text());
}

function loadMemberList() {
    const searchInput = document.getElementById("memberSearchInput");
    const searchVal = searchInput ? searchInput.value.trim().toLowerCase() : "";
    const container = document.getElementById("memberList");

    if (!container) return;

    getMembers()
        .then(html => {
            const temp = document.createElement("div");
            temp.innerHTML = html;
            const table = temp.querySelector(".bookTable");

            if (!table) {
                container.innerHTML = `
                    <table class="bookTable">
                        <thead>
                            <tr><th>S.No</th><th>Name</th><th>Phone No</th><th>Alternative Phone No</th><th>Address</th><th>Action</th></tr>
                        </thead>
                        <tbody>
                            <tr><td colspan="6" class="empty-table-cell">No active members.</td></tr>
                        </tbody>
                    </table>`;
                return;
            }

            const rows = Array.from(table.querySelectorAll("tbody .bookItem"));

            const filtered = searchVal
                ? rows.filter(item => item.textContent.toLowerCase().includes(searchVal))
                : rows;

            const updatedTable = table.cloneNode(true);
            const updatedBody = updatedTable.querySelector("tbody");

            if (filtered.length === 0) {
                const message = searchVal ? "No matching members found." : "No active members.";
                updatedBody.innerHTML = `<tr><td colspan="6" class="empty-table-cell">${message}</td></tr>`;
            } else {
                updatedBody.innerHTML = filtered.map(item => item.outerHTML).join("");
            }
            container.innerHTML = updatedTable.outerHTML;
        })
        .catch(error => {
            container.innerHTML = "<p>Could not reach the server.</p>";
            console.error(error);
        });
}

function searchMembers() {
    loadMemberList();
}

function deleteMember(name, phone) {
    if (!confirm(`Are you sure you want to delete member: ${name}?`)) return;

    fetch(SERVER_URL + "/deleteMember", {
        method: "POST",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, phone })
    })
        .then(response => response.text())
        .then(text => {
            showMessage(text);
            loadMemberList();
        })
        .catch(error => {
            showMessage("Could not delete member.");
            console.error(error);
        });
}

function initTheme() {
    const themeToggle = document.getElementById("themeToggle");
    if (!themeToggle) return;

    const savedTheme = localStorage.getItem("theme") || "light";
    document.documentElement.setAttribute("data-theme", savedTheme);
    updateThemeIcon(savedTheme);

    themeToggle.addEventListener("click", () => {
        const currentTheme = document.documentElement.getAttribute("data-theme") || "light";
        const newTheme = currentTheme === "dark" ? "light" : "dark";
        
        document.documentElement.setAttribute("data-theme", newTheme);
        localStorage.setItem("theme", newTheme);
        updateThemeIcon(newTheme);
    });
}

function updateThemeIcon(theme) {
    const themeToggleIcon = document.getElementById("themeToggleIcon");
    if (!themeToggleIcon) return;

    if (theme === "dark") {
        themeToggleIcon.innerHTML = `<path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z"/>`;
    } else {
        themeToggleIcon.innerHTML = `<circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M6.34 17.66l-1.41 1.41M19.07 4.93l-1.41 1.41"/>`;
    }
}

function openAboutModal() {
    const modal = document.getElementById("aboutModal");
    if (modal) {
        modal.classList.add("show");
    }
}

function closeAboutModal() {
    const modal = document.getElementById("aboutModal");
    if (modal) {
        modal.classList.remove("show");
    }
}

window.addEventListener("DOMContentLoaded", () => {
    initTheme();
    const sidebar = document.querySelector('.sidebar');
    const sidebarToggle = document.getElementById('sidebarToggle');
    const aboutToggle = document.getElementById('aboutToggle');

    if (sidebarToggle && sidebar) {
        sidebarToggle.addEventListener('click', () => {
            sidebar.classList.toggle('collapsed');
        });
    }

    if (aboutToggle) {
        aboutToggle.addEventListener('click', openAboutModal);
    }

    document.querySelectorAll('.menu-item').forEach(button => {
        button.addEventListener('click', () => {
            const viewName = button.dataset.view;
            if (!viewName) return; // Do not switch views for the toggle button
            switchView(viewName);
            if (viewName === 'books') {
                const searchInput = document.getElementById("searchInput");
                if (searchInput) searchInput.value = "";
                loadBookList();
            } else if (viewName === 'borrowers') {
                const bSearchInput = document.getElementById("borrowerSearchInput");
                if (bSearchInput) bSearchInput.value = "";
                loadBorrowerList();
            } else if (viewName === 'members') {
                const mSearchInput = document.getElementById("memberSearchInput");
                if (mSearchInput) mSearchInput.value = "";
                loadMemberList();
            }
        });
    });

    attachEventHandlers();
    loadBookList();
    loadBorrowerList();
    loadMemberList();
});