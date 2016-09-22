$('#datetimepicker').datetimepicker({
    language: 'zh-CN'
});

$(function () {
    $('#container').highcharts({
        title: {
            text: '攻击情况分析'
        },
        xAxis: {
            categories: ['18：00', '19：00', '20：00', '21：00', '22：00']
        },
        labels: {
            items: [{
                html: 'Total',
                style: {
                    left: '50px',
                    top: '18px',
                    color: (Highcharts.theme && Highcharts.theme.textColor) || 'black'
                }
            }]
        },
        series: [{
            type: 'column',
            name: 'TCP Flood',
            data: [3, 2, 1, 3, 4]
        }, {
            type: 'column',
            name: 'ICMP Echo',
            data: [2, 3, 5, 7, 6]
        }, {
            type: 'column',
            name: 'UDP Flood',
            data: [4, 3, 3, 9, 0]
        }, {
            type: 'spline',
            name: 'Average',
            data: [3, 2.67, 3, 6.33, 3.33],
            marker: {
                lineWidth: 2,
                lineColor: Highcharts.getOptions().colors[3],
                fillColor: 'white'
            }
        }, {
            type: 'pie',
            name: 'Total',
            data: [{
                name: 'TCP Flood',
                y: 13,
                color: Highcharts.getOptions().colors[0] // Jane's color
            }, {
                name: 'ICMP Echo',
                y: 23,
                color: Highcharts.getOptions().colors[1] // John's color
            }, {
                name: 'UDP Flood',
                y: 19,
                color: Highcharts.getOptions().colors[2] // Joe's color
            }],
            center: [100, 80],
            size: 100,
            showInLegend: false,
            dataLabels: {
                enabled: false
            }
        }]
    });
});
//     function format(d) {
//     // `d` is the original data object for the row
//     return '<table cellpadding="5" cellspacing="0" border="0" style="padding-left:50px;">' +
//         '<tr>' +
//         '<td>Ethernet:</td>' +
//         '</tr>' +
//         '<tr>' +
//         '<td>Src:</td>' +
//         '<td>' + d.ethernet.src + '</td>' +
//         '</tr>' +
//         '<tr>' +
//         '<td>Dst:</td>' +
//         '<td>' + d.ethernet.dst + '</td>' +
//         '</tr>' +
//         '<tr>' +
//         '<td>Length:</td>' +
//         '<td>' + d.ethernet.length + '</td>' +
//         '</tr>' +
//         '<tr>' +
//         '<td>Offset:</td>' +
//         '<td>' + d.ethernet.offset + '</td>' +
//         '</tr>' +
//         '</table>';
// }
//
// function format1(d) {
//     // `d` is the original data object for the row
//     return '<table cellpadding="5" cellspacing="0" border="0" style="padding-left:50px;">' +
//         '<tr>' +
//         '<td>Ip:</td>' +
//         '</tr>' +
//         '<tr>' +
//         '<td>Src:</td>' +
//         '<td>' + d.ip.src + '</td>' +
//         '</tr>' +
//         '<tr>' +
//         '<td>Dst:</td>' +
//         '<td>' + d.ip.dst + '</td>' +
//         '</tr>' +
//         '<tr>' +
//         '<td>Length:</td>' +
//         '<td>' + d.ip.length + '</td>' +
//         '</tr>' +
//         '<tr>' +
//         '<td>Offset:</td>' +
//         '<td>' + d.ip.offset + '</td>' +
//         '</tr>' +
//         '</table>';
// }
//
// function format2(d) {
//     // `d` is the original data object for the row
//     return '<table cellpadding="5" cellspacing="0" border="0" style="padding-left:50px;">' +
//         '<tr>' +
//         '<td>Tcp:</td>' +
//         '</tr>' +
//         '<tr>' +
//         '<td>Src:</td>' +
//         '<td>' + d.tcp.src + '</td>' +
//         '</tr>' +
//         '<tr>' +
//         '<td>Dst:</td>' +
//         '<td>' + d.tcp.dst + '</td>' +
//         '</tr>' +
//         '<tr>' +
//         '<td>Ack:</td>' +
//         '<td>' + d.tcp.ack + '</td>' +
//         '</tr>' +
//         '<tr>' +
//         '<td>Rst:</td>' +
//         '<td>' + d.tcp.rst + '</td>' +
//         '</tr>' +
//         '<tr>' +
//         '<td>Syn:</td>' +
//         '<td>' + d.tcp.syn + '</td>' +
//         '</tr>' +
//         '<tr>' +
//         '<td>Fin:</td>' +
//         '<td>' + d.tcp.fin + '</td>' +
//         '</tr>' +
//         '</table>';
// }
// $(document).ready(function () {
// var table = $('#example').DataTable({
//     destroy: true,
//     "ajax": "/api/attacks",
//     "columns": [
//         {"data": 'timestamp'},
//         {"data": "caplen"},
//         {"data": "wirelen"},
//         {
//             "className": 'details-control',
//             "orderable": false,
//             "data": null,
//             "defaultContent": ''
//         },
//         {
//             "className": 'details-control-1',
//             "orderable": false,
//             "data": null,
//             "defaultContent": ''
//         },
//         {
//             "className": 'details-control-2',
//             "orderable": false,
//             "data": null,
//             "defaultContent": ''
//         }
//     ],
//     "order": [[1, 'asc']]
// });
// // Add event listener for opening and closing details
// $('#example tbody').on('click', 'td.details-control', function () {
//     var tr = $(this).closest('tr');
//     var row = table.row(tr);
//
//     if (row.child.isShown()) {
//         // This row is already open - close it
//         row.child.hide();
//         $(this).removeClass('shown');
//     }
//     else {
//         // Open this row
//
//         row.child(format(row.data())).show();
//         $(this).addClass('shown');
//     }
// });
//
// $('#example tbody').on('click', 'td.details-control-1', function () {
//     var tr = $(this).closest('tr');
//     var row = table.row(tr);
//
//     if (row.child.isShown()) {
//         // This row is already open - close it
//         row.child.hide();
//         $(this).removeClass('shown');
//     }
//     else {
//         if (row.data().ip) {
//             // Open this row
//             row.child(format1(row.data())).show();
//             $(this).addClass('shown');
//         } else {
//             alert("none");
//         }
//     }
// });
//
// $('#example tbody').on('click', 'td.details-control-2', function () {
//     var tr = $(this).closest('tr');
//     var row = table.row(tr);
//
//     if (row.child.isShown()) {
//         // This row is already open - close it
//         row.child.hide();
//         $(this).removeClass('shown');
//     }
//     else {
//         // Open this row
//         if (row.data().tcp) {
//             row.child(format2(row.data())).show();
//             $(this).addClass('shown');
//         } else {
//             alert("none");
//         }
//     }
// });

var attacks_table = $('#attacks').DataTable({
    "ajax": "/api/attacks",
    "columns": [
        {"data": 'BeginTime'},
        {"data": "Duration"},
        {"data": "Attacker"},
        {"data": "Victim"},
        {"data": "Protocol"},
        {"data": "Description"}
    ]
});
var attack_table = $('#attack').DataTable({
    "ajax": "/api/fh_attacks",
    "columns": [
        {"data": 'Attacker'},
        {"data": "Victim"},
        {"data": "Protocol"},
        {"data": "Description"}
    ],
    "order": [[1, 'asc']]
});

var flow_table = $('#example').DataTable({
    // destroy: true,
   "ajax": '/api/flows',
    "columns": [
        {"data": 'BeginTime'},
        {"data": 'EndTime'},
        {"data": 'SrcIP'},
        {"data": 'SrcPort'},
        {"data": 'DstIP'},
        {"data": 'DstPort'},
        {"data": 'Type'},
        {"data": 'PacketNum'},
        {"data": 'PacketSize'}
    ]
});
// var attack2_table = $('#tp_attack').DataTable({
//    "ajax": "/api/tp_attacks",
//     "columns": [
//         {"data": ''},
//         {"data": ''},
//         {"data": ''},
//         {"data": ''}
//     ]
// });

var src_attack = $('#src_attack').DataTable({
   "ajax": "/api/src_attacks",
    "columns": [
        {"data": 'BeginTime'},
        {"data": 'Attacker'},
        {"data": 'Protocol'},
        {"data": 'Flows/s'},
        {"data": 'Packets/s'},
        {"data": 'Bytes/s'}
    ]
});


var dst_attack = $('#dst_attack').DataTable({
    "ajax": "/api/dst_attacks",
    "columns": [
        {"data": 'BeginTime'},
        {"data": 'Victim'},
        {"data": 'Protocol'},
        {"data": 'Flows/s'},
        {"data": 'Packets/s'},
        {"data": 'Bytes/s'}
    ]
});

var socket = io("http://localhost:3000");
socket.on('attack', function(data) {
    console.log(data);
    $('.bw').prepend($('<div>').attr("class","alert alert-success").attr("role","alert").text(data.hello));
});