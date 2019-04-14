const url = '/api/validate'
const form = document.querySelector('form')

form.addEventListener('submit', e => {
  e.preventDefault()

  const files = document.querySelector('[type=file]').files
  const formData = new FormData()

  for (let i = 0; i < files.length; i++) {
    let file = files[i]
    formData.append('file', file)
  }
    console.log("New File" + formData)

  fetch(url, {
    method: 'POST',
    body: formData,
  }).then(function(response) {
    return response.json()
  }).then(function(myJson) {
    console.log(myJson);
    displayResult(myJson)
  });

})

function displayResult(json) {
    displayTable(true, json.seal)

    for (i in json.signatureList) {
        displayTable(false, json.signatureList[i])
    }

    document.getElementById("result").insertAdjacentHTML('beforeend',
        "<code>" + json.data + "</code>");
}

function displayTable(seal, signature) {
    result = document.getElementById("result")
    var tblStr = "<table class=\"example-table\">"
    if(seal) {
        tblStr += "<tr><td colpan=\"2\"><b>Seal</b></td></tr>"
    } else {
        tblStr += "<tr><td colpan=\"2\"><b>Signature</b></td></tr>"
    }
    tblStr += "" +
        getTableRow("Ssn", (signature.ssn == null ? "" : signature.ssn)) +
        getTableRow("Cn", (signature.cn == null ? "" : signature.cn)) +
        getTableRow("Organisasjonsnummer", (signature.orgnumber === null ? "" : signature.orgnumber)) +
        getTableRow("O", (signature.o === null ? "" : signature.o)) +
        getTableRow("pkiVendor", (signature.pkiVendor === null ? "" : signature.pkiVendor)) +
        getTableRow("policy", (signature.policy === null ? "" : signature.policy)) +
        getTableRow("uniqueId", (signature.uniqueId === null ? "" : signature.uniqueId)) +
        getTableRow("From", (signature.from === null ? "" : new Date(signature.from))) +
        getTableRow("To", (signature.to === null ? "" : new Date(signature.to))) +
        "</table><p></p>";
    result.insertAdjacentHTML('beforeend', tblStr)

}

function getTableRow(header, v) {
    return "<tr><td>" + header + "</td><td>" + v + "</td></tr>"
}
