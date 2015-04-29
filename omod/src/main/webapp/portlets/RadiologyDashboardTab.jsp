<%-- 
    Document   : RadiologyDashboardTab
    Created on : Apr 26, 2013, 2:51:31 PM
    Author     : Akhil
--%>

<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:htmlInclude file="/scripts/jquery/jquery.min.js" />
<openmrs:htmlInclude file="/dwr/engine.js"></openmrs:htmlInclude>
<openmrs:htmlInclude file="/dwr/util.js"></openmrs:htmlInclude>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<openmrs:htmlInclude
	file="/scripts/jquery/dataTables/css/dataTables.css" />
<openmrs:htmlInclude
	file="/scripts/jquery-ui/js/jquery-ui-1.7.2.custom.min.js" />
<openmrs:htmlInclude
	file="/scripts/jquery-ui/css/redmond/jquery-ui-1.7.2.custom.css" />

<openmrs:htmlInclude file="/moduleResources/radiology/radiology.css" />
<openmrs:htmlInclude
	file="/moduleResources/radiology/js/jquery.dataTables.min.js" />
<%@ include
	file="/WEB-INF/view/module/radiology/resources/js/orderList.js"%>
<openmrs:htmlInclude file="/moduleResources/radiology/js/sortNumbers.js" />
<openmrs:htmlInclude file="/moduleResources/radiology/css/ColVis.css" />
<openmrs:htmlInclude file="/moduleResources/radiology/js/ColVis.min.js" />

<openmrs:hasPrivilege privilege="Add Orders">
	<p>
		<a href="module/radiology/radiologyOrder.form?patientId=${patientId}"><spring:message
				code="radiology.addOrder" /></a> <br />
	</p>
</openmrs:hasPrivilege>

<div id="radiologyOrders">
	<div id="radiologyHeader" class="boxHeader">Patient Radiology
		Orders</div>
	<div id="radiologyTable" class="box">
		<p></p>
		<c:if test="${empty studyList}">
			<p>No Radiology Orders Present.</p>
		</c:if>
		<c:if test="${not empty studyList}">
			<table id="matchedOrders" cellpadding="2" cellspacing="0"
				width="100%">
				<thead>
					<tr>
						<th><c:if test="${empty obsId}">
								<spring:message code="general.edit" />
							</c:if> <c:if test="${not empty obsId}">View Obs.</c:if></th>
						<th><spring:message code="radiology.priority" /></th>
						<th><spring:message code="radiology.referringPhysician" /></th>
						<th><spring:message code="radiology.scheduler" /></th>
						<th><spring:message code="radiology.performingPhysician" /></th>
						<th><spring:message code="radiology.readingPhysician" /></th>
						<th><spring:message code="radiology.appoinmentDate" /></th>
						<th><spring:message code="radiology.modality" /></th>
						<th title="<spring:message code="radiology.accordingModality"/>">
							<spring:message code="radiology.status" />
						</th>
						<th><spring:message code="general.instructions" /></th>
					</tr>
				</thead>
				<tbody id="matchedOrdersBody">
					<c:forEach items="${studyList}" var="study" varStatus="i" begin="0">
						<tr>
							<td style="text-align: center"><c:if test="${empty obsId}">
									<a href="radiologyOrder.form?orderId=${study.order.orderId}">${i.count}</a>
								</c:if> <c:if test="${not empty obsId}">
									<!-- TODO  select observation-->
									<a
										href="module/radiology/portlets/radiologyObsDashboard.form?orderId=${study.order.orderId}"
										style="cursor: pointer;" class="viewRadiologyObservations">${i.count}</a>
								</c:if></td>
							<td>${priorities[i.count-1]}</td>
							<td>${study.order.orderer.personName}</td>
							<td>${schedulers[i.count-1]}</td>
							<td>${performings[i.count-1]}</td>
							<td>${readings[i.count-1]}</td>
							<td>${study.order.startDate}</td>
							<td>${study.modality.fullName}</td>
							<td>${statuses[i.count-1]}</td>
							<td style="max-width: 90px; overflow: hidden;"><a
								style="cursor: pointer"
								onclick="$j('<p>'+this.innerHTML+'</p>').dialog({autoOpen:true,modal:true});"
								title="<spring:message code="general.view"/>">${study.order.instructions}
							</a></td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</c:if>

	</div>
</div>
<div id="viewRadiologyObservationsPopup"></div>

<script type="text/javascript">
	var $j = jQuery.noConflict();
	$j(document)
			.ready(
					function() {
						$j('#viewRadiologyObservationsPopup')
								.dialog(
										{
											autoOpen : false,
											modal : true,
											position : top,
											title : '<spring:message code="radiology.radiologyObservations" javaScriptEscape="true"/>',
											width : '90%',
										});
						$j('.viewRadiologyObservations').click(
								function(e) {
									e.preventDefault();
									var url = $j(this).attr('href');
									$j('#viewRadiologyObservationsPopup').load(
											url);
									$j('#viewRadiologyObservationsPopup')
											.dialog('open');
								});

						$j('table#matchedOrders')
								.dataTable(
										{
											"iDisplayLength" : 20,
											"aLengthMenu" : [ 20, 50 ],
											"sPaginationType" : 'full_numbers',
											"bJQueryUI" : true,
											"bAutoWidth" : false,
											"sDom" : '<"H"<"tableHeader verticalCentered"<"left"l><"center"fr><"right colVisDiv"C>>>'
													+ 't<"F" <"verticalCentered"<"left"i><"right"p>>>',
											"oColVis" : {
												"buttonText" : '<spring:message code="radiology.showHideColumns"/>',
												"aiExclude" : [ 0 ]
											},
											"aoColumnDefs" : [ {
												"sType" : "num-html",
												"bSortable" : true,
												"aTargets" : [ 0 ]
											} ],
											"fnDrawCallback" : function() {

												$j(
														'#actionSelect > option[value="-1"]')
														.hide();
												$j('#actionSelect')
														.change(
																function() {
																	action = $j(
																			'#actionSelect')
																			.attr(
																					'selectedIndex');
																	if (action == 1) {
																		$j(
																				'#voidReasonPopup')
																				.dialog(
																						'open');
																	}
																	$j(
																			'#actionSelect')
																			.attr(
																					'selectedIndex',
																					'0');
																});
												$j('div.right.colVisDiv')
														.click(
																function() {
																	style = $j(
																			'div#footer + div + div')
																			.attr(
																					'style');
																	$j(
																			'div#footer + div + div')
																			.attr(
																					'style',
																					style
																							+ ' width:103%;');
																});

												//********************** DT Events *******************							

												$j('#markAll')
														.click(
																function() {
																	$j(
																			'td#actionCheckboxes [type="checkbox"]')
																			.attr(
																					'checked',
																					true);
																});

												$j('#markNone')
														.click(
																function() {
																	$j(
																			'td#actionCheckboxes [type="checkbox"]')
																			.attr(
																					'checked',
																					false);
																});
											},
											"oLanguage" : {
												"sLengthMenu" : '<spring:message code="radiology.show"/>'
														+ ' _MENU_ <spring:message code="radiology.entries"/>',
												"sSearch" : '<spring:message code="general.search"/>:',
												"sInfo" : '<spring:message code="radiology.viewing"/> _START_ '
														+ '- _END_ '
														+ '<spring:message code="radiology.of"/> _TOTAL_',
												"oPaginate" : {
													"sFirst" : '<spring:message code="radiology.first"/>',
													"sPrevious" : '<spring:message code="general.previous"/>',
													"sNext" : '<spring:message code="general.next"/>',
													"sLast" : '<spring:message code="radiology.last"/>',
												},
												"sProcessing" : '<spring:message code="general.loading"/>'
											}
										});
					});
</script>
