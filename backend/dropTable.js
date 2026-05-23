const { Client } = require('pg');
const client = new Client({
  connectionString: 'postgresql://postgres.spnclqvzmhhrwegcknvs:mdqUEUEPASS1021@aws-1-ap-south-1.pooler.supabase.com:6543/postgres'
});
async function dropTable() {
  try {
    await client.connect();
    await client.query('DROP TABLE IF EXISTS appointments CASCADE;');
    console.log('Appointments table dropped successfully');
  } catch (err) {
    console.error('Error:', err);
  } finally {
    await client.end();
  }
}
dropTable();
