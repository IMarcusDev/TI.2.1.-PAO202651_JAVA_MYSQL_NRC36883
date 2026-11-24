<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%-- Consultar país en la API (ECUD3): resultados con «¿Guardado?» y un botón Guardar por fila. --%>
<c:set var="titulo" value="Consultar país en la API"/>
<c:set var="pantalla" value="consultar"/>
<%@ include file="partes/inicio.jspf" %>

<h1 class="titulo">Consultar país en la API</h1>
<p class="descripcion">Consulta REST Countries por nombre (por ejemplo «ecuador») y guarde el país elegido.</p>
<%@ include file="partes/avisos.jspf" %>

<form method="get" action="${ctx}/consultar" class="fila-formulario" novalidate>
    <div class="campo">
        <label for="nombre">Nombre del país</label>
        <input id="nombre" name="nombre" value="<c:out value='${nombre}'/>" autocomplete="off">
    </div>
    <button class="btn" type="submit">Consultar</button>
</form>

<c:if test="${not empty resultados}">
    <p class="total">${resultados.size()} ${resultados.size() == 1 ? 'resultado' : 'resultados'}</p>
    <div class="tabla-contenedor">
        <table class="tabla">
            <thead>
            <tr>
                <th scope="col">Nombre</th>
                <th scope="col">Capital</th>
                <th scope="col">Región</th>
                <th scope="col">Subregión</th>
                <th scope="col" class="numero">Población</th>
                <th scope="col" class="numero">Área (km²)</th>
                <th scope="col">Moneda</th>
                <th scope="col">¿Guardado?</th>
                <th scope="col"><span class="solo-lectores">Acción</span></th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="p" items="${resultados}" varStatus="fila">
                <tr>
                    <td><c:out value="${p.nombre}"/></td>
                    <td><c:out value="${p.capital}"/></td>
                    <td><c:out value="${p.region}"/></td>
                    <td><c:out value="${p.subregion}"/></td>
                    <td class="numero"><fmt:formatNumber value="${p.poblacion}" maxFractionDigits="0"/></td>
                    <td class="numero"><fmt:formatNumber value="${p.area}" maxFractionDigits="2"/></td>
                    <td><c:out value="${p.moneda}"/></td>
                    <td><span class="pastilla ${p.guardado ? 'pastilla--si' : 'pastilla--no'}">${p.guardado ? 'Sí' : 'No'}</span></td>
                    <td class="acciones">
                        <form method="post" action="${ctx}/consultar" class="en-linea">
                            <%@ include file="partes/csrf.jspf" %>
                            <input type="hidden" name="numero" value="${fila.count}">
                            <button class="btn btn--pequeno" type="submit">Guardar</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</c:if>

<%@ include file="partes/fin.jspf" %>
