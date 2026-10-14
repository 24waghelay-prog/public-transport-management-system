import { startTunnel } from 'untun';
import fs from 'fs';
import path from 'path';

const PORT = parseInt(process.env.PORT || '8085', 10);
const logDir = path.resolve('logs');
if (!fs.existsSync(logDir)) {
    fs.mkdirSync(logDir, { recursive: true });
}

console.log(`[TUNNEL] Establishing Cloudflare Tunnel to http://127.0.0.1:${PORT}...`);

try {
    const tunnel = await startTunnel({ hostname: '127.0.0.1', port: PORT });
    const url = await tunnel.getURL();
    const urlFile = path.join(logDir, 'public-tunnel-url.txt');

    fs.writeFileSync(urlFile, url, 'utf-8');
    console.log(`[TUNNEL] ========================================================`);
    console.log(`[TUNNEL] PUBLIC ACCESS URL: ${url}`);
    console.log(`[TUNNEL] URL saved to: ${urlFile}`);
    console.log(`[TUNNEL] Accessible from anywhere in the world, even on mobile!`);
    console.log(`[TUNNEL] ========================================================`);

    // Handle clean termination
    process.on('SIGINT', async () => {
        console.log('[TUNNEL] Closing tunnel...');
        try { await tunnel.close(); } catch (e) {}
        process.exit(0);
    });
    process.on('SIGTERM', async () => {
        console.log('[TUNNEL] Closing tunnel...');
        try { await tunnel.close(); } catch (e) {}
        process.exit(0);
    });
} catch (err) {
    console.error('[TUNNEL] Failed to start tunnel:', err.message);
    process.exit(1);
}
