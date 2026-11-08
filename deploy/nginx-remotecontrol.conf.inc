location = /remotecontrol/getZegoToken {
    client_max_body_size 8k;
    proxy_connect_timeout 2s;
    proxy_send_timeout 8s;
    proxy_read_timeout 8s;
    proxy_set_header Host $host;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_pass http://127.0.0.1:8088/getZegoToken;
}

location = /remotecontrol/health {
    proxy_connect_timeout 2s;
    proxy_read_timeout 3s;
    proxy_pass http://127.0.0.1:8088/health;
}
