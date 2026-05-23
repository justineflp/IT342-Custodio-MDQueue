import fs from 'fs';

console.log("Checking if backend is running...");
fetch("http://localhost:8080/api/appointments/all", {
    headers: {
        "Authorization": "Bearer fake",
    }
}).then(res => console.log(res.status)).catch(e => console.log("Backend not running?"));
