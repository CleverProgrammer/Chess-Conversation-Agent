function ButtonClass() {}

ButtonClass.setupButtons = function() {
  $("#annyangButton").click(function() {
    if (AnnyangClass.annyangStopped) {
      AnnyangClass.startAnnyang();
    } else {
      AnnyangClass.stopAnnyang();
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