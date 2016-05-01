window.config = {
    host: 'sa9us.herokuapp.com',
    http_port: 80,
    mqtt_port: 8080,
    topic: 'dog_sensor',
    isSSL: false,

    getHTTPString: function() {
        return 'http' + (this.isSSL ? 's' : '') + '://' + this.host + ':' + this.http_port;
    }
}
