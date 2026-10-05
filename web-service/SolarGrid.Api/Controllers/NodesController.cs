// -----------------------------------------------------------------------------
// File: NodesController.cs
// Author: Gunasena R. K. R. M. S. K.
// Purpose: System implementation for NodesController
// -----------------------------------------------------------------------------

using Microsoft.AspNetCore.Mvc;
using SolarGrid.Api.Models;
using SolarGrid.Api.Services;

namespace SolarGrid.Api.Controllers;

[ApiController]
[Route("api/nodes")]
public class NodesController : ControllerBase
{
    private readonly NodeService _nodeService;

    public NodesController(NodeService nodeService)
    {
        _nodeService = nodeService;
    }

    [HttpGet]
    // Inline comment: Expression body method execution
    public async Task<IActionResult> GetAll() =>
        Ok(await _nodeService.GetAllAsync());

    [HttpGet("{id}")]
    public async Task<IActionResult> GetById(string id)
    {
        // Inline comment: Method execution begins here.
        var node = await _nodeService.GetByIdAsync(id);
        if (node == null)
            return NotFound(new { message = "Node not found." });

        return Ok(node);
    }

    [HttpPost]
    public async Task<IActionResult> Create([FromBody] CreateNodeRequest request)
    {
        // Inline comment: Method execution begins here.
        var node = await _nodeService.CreateAsync(request);
        return CreatedAtAction(nameof(GetById), new { id = node.Id }, node);
    }

    [HttpPut("{id}")]
    public async Task<IActionResult> Update(string id, [FromBody] UpdateNodeRequest request)
    {
        // Inline comment: Method execution begins here.
        var node = await _nodeService.UpdateAsync(id, request);
        if (node == null)
            return NotFound(new { message = "Node not found." });

        return Ok(node);
    }

    [HttpPatch("{id}/deactivate")]
    public async Task<IActionResult> Deactivate(string id)
    {
        // Inline comment: Method execution begins here.
        try
        {
            var success = await _nodeService.DeactivateAsync(id);
            if (!success)
                return NotFound(new { message = "Node not found." });

            return Ok(new { message = "Node deactivated successfully." });
        }
        catch (ArgumentException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
    }
}
