<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%-- Reportes (ECUD6): país más poblado y dos tablas por región. --%>
<c:set var="titulo" value="Reportes"/>
<c:set var="pantalla" value="reportes"/>
<%@ include file="partes/inicio.jspf" %>

<h1 class="titulo">Reportes</h1>
<p class="descripcion">Estadísticas calculadas sobre los países almacenados.</p>
<%@ include file="partes/avisos.jspf" %>

<a class="btn btn--secundario" href="${ctx}/reportes">Actualizar</a>

<c:if test="${not empty reporte}">
    <div class="tarjeta" style="margin-top:16px">
        <h2 class="tarjeta__titulo">País más poblado</h2>
        <c:choose>
            <c:when test="${not empty reporte.paisMasPoblado}">
                <span class="cifra__valor"><c:out value="${reporte.paisMasPoblado.nombre}"/></span>
                <span class="cifra__etiqueta"><fmt:formatNumber value="${reporte.paisMasPoblado.poblacion}" maxFractionDigits="0"/>
                    habitantes · <c:out value="${reporte.paisMasPoblado.region}"/></span>
            </c:when>
            <c:otherwise><span class="cifra__valor">—</span></c:otherwise>
        </c:choose>
    </div>

    <div class="cifras" style="margin-top:8px; align-items:start">
        <section>
            <h2 class="subtitulo">Cantidad de países por región</h2>
            <div class="tabla-contenedor">
                <table class="tabla">
                    <thead><tr><th scope="col">Región</th><th scope="col" class="numero">Países</th></tr></thead>
                    <tbody>
                    <c:forEach var="fila" items="${reporte.paisesPorRegion}">
                        <tr><td><c:out value="${fila.region}"/></td>
                            <td class="numero"><fmt:formatNumber value="${fila.cantidad}"/></td></tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </section>
        <section>
            <h2 class="subtitulo">Promedio poblacional por región</h2>
            <div class="tabla-contenedor">
                <table class="tabla">
                    <thead><tr><th scope="col">Región</th><th scope="col" class="numero">Promedio de población</th></tr></thead>
                    <tbody>
                    <c:forEach var="fila" items="${reporte.promedioPorRegion}">
                        <tr><td><c:out value="${fila.region}"/></td>
                            <td class="numero"><fmt:formatNumber value="${fila.promedioPoblacion}" maxFractionDigits="0"/></td></tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </section>
    </div>
</c:if>

<%@ include file="partes/fin.jspf" %>
