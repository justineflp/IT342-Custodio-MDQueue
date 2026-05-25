const { Client } = require('pg');
const client = new Client({
  connectionString: 'postgresql://postgres.spnclqvzmhhrwegcknvs:mdqUEUEPASS1021@aws-1-ap-south-1.pooler.supabase.com:6543/postgres'
});
async function queryUsers() {
  try {
    await client.connect();
    const res = await client.query('SELECT id, email, full_name, role FROM users;');
    console.log('Users in database:', res.rows);
  } catch (err) {
    console.error('Error:', err);
  } finally {
    await client.end();
  }
}
queryUsers();
