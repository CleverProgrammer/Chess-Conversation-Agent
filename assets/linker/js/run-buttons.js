function setupButtons() {
  $("#annyangButton").click(function() {
    if (annyangStopped) {
      startAnnyang();
    } else {
      stopAnnyang();
    }
  });

  // Add listener to moveButton
  $("#moveButton").click(function() {
    // Get move from moveInput
    var move = $("#moveInput").val();
    console.log(move);

    // Display move on board
    board.move(move);
  });
}