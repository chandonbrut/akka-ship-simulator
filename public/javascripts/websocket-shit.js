function testWebSocket(wsUrl) {
    console.log('Vou me loggar em ' + wsUrl);
    websocket = new WebSocket(wsUrl);
    websocket.onopen = function(evt) { onOpen(evt) };
    websocket.onclose = function(evt) { onClose(evt) };
    websocket.onmessage = function(evt) { onMessage(evt) };
    websocket.onerror = function(evt) { onError(evt) };
  }

  function onOpen(evt) {
    doSend("register");
  }

  function onClose(evt) {

  }

  function onError(evt){ }

  function doSend(message) {
    websocket.send(message);
  }