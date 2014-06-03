function ButtonClass() {}

ButtonClass.setupButtons = function() {
  $("#annyangButton").click(function() {
    if (AnnyangClass.annyangStopped) {
      AnnyangClass.startAnnyang();
    } else {
      AnnyangClass.stopAnnyang();
    }
  });
}